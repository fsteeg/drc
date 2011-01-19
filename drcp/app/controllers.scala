package controllers

import com.quui.sinist.XmlDb
import de.uni_koeln.ub.drc.data.User
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
    val pages = ids.take(2).map("http://" + server + "/exist/rest/db/drc/" + collection + "/" +
      _.replace(".xml", ".jpg"))

    Template(active, inactive, ids, pages)
  }

}
