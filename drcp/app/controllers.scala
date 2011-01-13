package controllers

import com.quui.sinist.XmlDb
import de.uni_koeln.ub.drc.data.User
import play._
import play.mvc._

object Application extends Controller {

  val db = XmlDb("xmldb:exist://hydra1.spinfo.uni-koeln.de:8080/exist/xmlrpc", "db", "drc")

  def index = {
    val users = for (u <- db.getXml("users").get) yield User.fromXml(u)
    val (active, inactive) = users.sortBy(_.reputation).reverse.partition(_.reputation > 0)
    Template(active, inactive)
  }

}
