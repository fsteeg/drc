package controllers

import com.quui.sinist.XmlDb
import de.uni_koeln.ub.drc.data._
import play._
import play.mvc._
import play.data.validation._

object Application extends Controller {

  val server = "hydra1.spinfo.uni-koeln.de:8080"
  val db = XmlDb("xmldb:exist://" + server + "/exist/xmlrpc", "db", "drc")

  def index = {
    val users = for (u <- db.getXml("users").get) yield User.fromXml(u)
    val (active, inactive) = users.sortBy(_.reputation).reverse.partition(_.reputation > 0)

    val ids = db.getIds("PPN345572629_0004").get.filter(_.endsWith(".xml"))
    val pages = ids.take(5).map(imageLink(_))

    Template(active, inactive, ids, pages)
  }

  private def imageLink(id: String) = "http://" + server + "/exist/rest/db/drc/" + 
    (if(id.matches(".*?PPN345572629_0004-000[1-6].*?")) id /* temp workaround for old data */ else
    (if(id.contains("-")) id.substring(0,id.indexOf('-')) else id) + "/" + id.replace(".xml", ".png"))

  def user(id: String) = {
    val user = User.fromXml(db.getXml("users", id + ".xml").get(0))
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
      val u = User(id, name, region, pass, XmlDb("xmldb:exist://" + server + "/exist/xmlrpc", "db", "drc"))
      db.putXml(u.toXml, "users", id + ".xml")
      Action(user(id))
    }
  }

}
