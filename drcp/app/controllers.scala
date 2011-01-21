package controllers

import com.quui.sinist.XmlDb
import de.uni_koeln.ub.drc.data._
import play._
import play.mvc._

object Application extends Controller {

  val server = "hydra1.spinfo.uni-koeln.de:8080"
  val collection = "PPN345572629_0004"
  val db = XmlDb("xmldb:exist://" + server + "/exist/xmlrpc", "db", "drc")

  def index = {
    val users = for (u <- db.getXml("users").get) yield User.fromXml(u)
    val (active, inactive) = users.sortBy(_.reputation).reverse.partition(_.reputation > 0)

    val ids = db.getIds(collection).get.filter(_.endsWith(".xml"))
    val pages = ids.take(5).map(imageLink(_))

    Template(active, inactive, ids, pages)
  }
  
  private def imageLink(id:String) = "http://" + server + "/exist/rest/db/drc/" + collection + "/" +
      id.replace(".xml", ".jpg")

  def user(id: String) = {
    val user = User.fromXml(db.getXml("users", id+".xml").get(0))
    val link = imageLink(user.latestPage)
    val page = new Page(null, user.latestPage)
    Template(user, link, page)
  }

}
