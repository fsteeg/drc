package controllers

import com.quui.sinist.XmlDb
import de.uni_koeln.ub.drc.data._
import play._
import play.mvc._
import play.i18n.Lang
import play.data.validation._
import scala.xml.Elem
import scala.xml.Node
import scala.xml.NodeSeq

object Application extends Controller {

  val server = "hydra1.spinfo.uni-koeln.de"
  val port = 8080
  val db = XmlDb(server, port)
  val col = "drc"
	  
  def loadUsers =
	  (for (u <- db.getXml(col + "/users").get) yield User.fromXml(u))
	  .sortBy(_.reputation).reverse

  def index = {
	val top = loadUsers.take(5)
    //val ids = db.getIds(col + "/PPN345572629_0004").get.filter(_.endsWith(".xml"))
    //val pages = ids.take(5).map(imageLink(_))
	val ids=List()
    Template(top, ids/*, pages*/)
  }
  
  def contact = { Template() }
  def faq = { Template() }
  def info = { Template() }

  def users = {
	val all = loadUsers
	val (left, right) = all.splitAt(all.size/2)
    Template(left, right)
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
    val users = loadUsers
    if (Validation.hasErrors || (users).exists(_.id == id)) {
      "@signup".asTemplate
    } else {
      val u = User(id, name, region, pass, col, db)
      db.putXml(u.toXml, col + "/users", id + ".xml")
      Action(user(id))
    }
  }
  
  def changeLanguage(lang:String) = {
    Lang.change(lang)
    val refererURL = Http.Request.current().headers.get("referer").value 
    Redirect(refererURL)
  }
  
  def search(@Required term: String, @Required volume: String) = {
    val volumes = List("0004", "0008", "0009", "0011", "0012", "0017", "0018", "0024", "0027",
    		"0033", "0035", "0036", "0037", "0038")
    val vol = if(volume.toInt-1<volumes.size) "PPN345572629_" + volumes(volume.toInt-1) else ""
    val query = createQuery("/page", term)
    val q = db.query("drc-plain/" + vol, configure(query))
    val rows = (q\"tr")
    val links = rows.map((n:Node)=>imageLink((n\\"a"\"@href").text))
    val pages = for((row, link) <- rows zip links) yield {
    	val text = link.replace(".png", ".xml").replace("drc/", "drc-plain/")
    	<tr> {(row \ "td") ++ 
    		<td><a href={link}>image</a></td> ++ 
    		<td><a href={text}>text</a></td>} 
        </tr>
    } 
    Template(term, volume, pages)
  }
  
  def createQuery(selector: String, term:String) = {
      """
      import module namespace kwic="http://exist-db.org/xquery/kwic";
      declare option exist:serialize "omit-xml-declaration=no encoding=utf-8";
      for $m in %s[ft:query(., '%s')]
      order by ft:score($m) descending
      return kwic:summarize($m, <config width="40" table="yes" link="{$m/attribute::id}"/>)
      """.format(selector, term.toLowerCase)
  }
  
  private def configure(query: String): scala.xml.Elem = {
    val cdata = "<![CDATA[%s]]>".format(query)
    <query xmlns="http://exist.sourceforge.net/NS/exist" start="1" max="100">
      <text> { scala.xml.Unparsed(cdata) } </text>
      <properties> <property name="indent" value="yes"/> </properties>
    </query>
  }

}
