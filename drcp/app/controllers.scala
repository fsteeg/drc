package controllers

import com.quui.sinist.XmlDb
import de.uni_koeln.ub.drc.data._
import play._
import play.mvc._
import play.data.validation._
import scala.xml.Elem
import scala.xml.Node

object Application extends Controller {

  val server = "hydra1.spinfo.uni-koeln.de"
  val port = 8080
  val db = XmlDb(server, port)
  val col = "drc"
	  
  lazy val u =
	  (for (u <- db.getXml(col + "/users").get) yield User.fromXml(u))
	  .sortBy(_.reputation).reverse.partition(_.reputation > 0)

  def index = {
	val top = u._1.take(5)
    //val ids = db.getIds(col + "/PPN345572629_0004").get.filter(_.endsWith(".xml"))
    //val pages = ids.take(5).map(imageLink(_))
	val ids=List()
    Template(top, ids/*, pages*/)
  }
  
  def contact = { Template() }
  def faq = { Template() }
  def info = { Template() }

  def users = {
	val (active, inactive) = u
    Template(active, inactive)
  }

  private def imageLink(id: String) = "http://" + server + ":" + port + "/exist/rest/db/" + col + "/" +
    (if (id.matches(".*?PPN345572629_0004-000[1-6].*?")) id /* temp workaround for old data */ else
      (if (id.contains("-")) id.substring(0, id.indexOf('-')) else id) + "/" + id.replace(".xml", ".png"))

  def user(id: String) = {
    val user = User.fromXml(db.getXml(col + "/users", id + ".xml").get(0))
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
      db.putXml(u.toXml, col + "/users", id + ".xml")
      Action(user(id))
    }
  }
  
  def search(@Required term: String, @Required volume: String) = {
    val volumes = List("0004", "0008", "0009", "0011", "0012", "0017", "0018", "0024", "0027")
    val vol = if(volume.toInt-1<volumes.size) "PPN345572629_" + volumes(volume.toInt-1) else ""
    val selector = "//modification/attribute::form"
    val query = "for $m in %s[ft:query(., '%s')]/ancestor::page return string($m/attribute::id)".format(selector, term.toLowerCase)
    val q = db.query("drc/" + vol, configure(query))
    val pages = (q\"value").map((n:Node)=>imageLink(n.text))
    Template(term, volume, pages)
  }
  
  private def configure(query: String): scala.xml.Elem = {
    val cdata = "<![CDATA[%s]]>".format(query)
    <query xmlns="http://exist.sourceforge.net/NS/exist" start="1" max="20">
      <text> { scala.xml.Unparsed(cdata) } </text>
      <properties> <property name="indent" value="yes"/> </properties>
    </query>
  }

}
