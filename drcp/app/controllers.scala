package controllers

import com.quui.sinist.XmlDb
import de.uni_koeln.ub.drc.data._
import play._
import play.mvc._
import play.data.validation._

object Application extends Controller {

  val server = "localhost" //"hydra1.spinfo.uni-koeln.de"
  val port = 7777 //8080
  val db = XmlDb(server, port)
  val col = "drc"

  def index = {
    val users = for (u <- db.getXml(col+"/users").get) yield User.fromXml(u)
    val (active, inactive) = users.sortBy(_.reputation).reverse.partition(_.reputation > 0)

    val ids = db.getIds(col+"/PPN345572629_0004").get.filter(_.endsWith(".xml"))
    val pages = ids.take(5).map(imageLink(_))

    Template(active, inactive, ids, pages)
  }

  private def imageLink(id: String) = "http://" + server + ":" + port + "/exist/rest/db/" + col + "/" +
    (if(id.matches(".*?PPN345572629_0004-000[1-6].*?")) id /* temp workaround for old data */ else
    (if(id.contains("-")) id.substring(0,id.indexOf('-')) else id) + "/" + id.replace(".xml", ".png"))

  def user(id: String) = {
    val user = User.fromXml(db.getXml(col+"/users", id + ".xml").get(0))
    val link = imageLink(user.latestPage)
    val page = new Page(null, user.latestPage)
    Template(user, link, page)
  }

  def signup = Template

  def createAccount(@Required name: String, @Required id: String, @Required pass: String, @Required region: String) = {
    println("name: %s, id: %s, pass: %s, region: %s".format(name, id, pass, region))
    if (Validation.hasErrors) {
      "@signup".asTemplate
    } else {
      val u = User(id, name, region, pass, col, db)
      db.putXml(u.toXml, col+"/users", id + ".xml")
      Action(user(id))
    }
  }

}
