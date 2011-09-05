/**************************************************************************************************
 * Copyright (c) 2010, 2011 Fabian Steeg. All rights reserved. This program and the accompanying 
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies 
 * this distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: Fabian Steeg - initial API and implementation
 *************************************************************************************************/
package controllers

import com.quui.sinist.XmlDb
import de.uni_koeln.ub.drc.data._
import de.uni_koeln.ub.drc.util.MetsTransformer
import play._
import play.mvc._
import play.i18n.Lang
import play.i18n.Messages._
import play.data.validation._
import scala.xml.Elem
import scala.xml.Node
import scala.xml.NodeSeq
import scala.xml.XML
import scala.xml.Unparsed

object Application extends Controller with Secure {
  
  import views.Application._

  val server = "hydra1.spinfo.uni-koeln.de"
  val port = 8080
  val db = XmlDb(server, port)
  val col = "drc"

  def loadUsers =
    (for (u <- db.getXml(col + "/users").get) yield User.fromXml(u))
      .sortBy(_.reputation).reverse

  def index = {
    val top = loadUsers.take(5)
    html.index(top)
  }
  
  def edit = html.edit(User.withId(col, db, currentUser))
  
  private def currentUser = session.get("username")

  def contact = html.contact()
  def faq = html.faq()
  def info = html.info()
  def press = html.press()
  def salid = html.salid()

  def users = {
    val all = loadUsers
    val (left, right) = all.splitAt(all.size / 2)
    html.users(left, right)
  }

  private def imageLink(id: String) = "http://" + server + ":" + port + "/exist/rest/db/" + col + "/" +
    (if (id.matches(".*?PPN345572629_0004-000[1-6].*?")) id /* temp workaround for old data */ else
      (if (id.contains("-")) id.substring(0, id.indexOf('-')) else id) + "/" + id.replace(".xml", ".png"))
      
  private def textLink(link: String) = link.replace(".png", ".xml").replace("drc/", "drc-plain/")

  def user(id: String) = {
    val user:User = User.withId(col, db, id)
    val link:String = imageLink(user.latestPage)
    val page:Page = new Page(null, user.latestPage)
    html.user(user, link, page)
  }

  def signup = html.signup()

  def account() = {
    val name = params.get("name")
    val id = params.get("id")
    val pass = params.get("pass")
    val region = params.get("region")
    val message = get("views.signup.error")
    Validation.required("name", name).message(message)
    Validation.required("id", id).message(message)
    Validation.required("pass", pass).message(message)
    Validation.required("region", region).message(message)
    createAccount(name, id, pass, region)
  }
  
  def createAccount(@Required name: String, @Required id: String, @Required pass: String, @Required region: String) = {
    println("signup; name: %s, id: %s, pass: %s, region: %s".format(name, id, pass, region))
    val users = loadUsers
    val changeExistingAccount = params.get("change") != null
    if (Validation.hasErrors || ((users).exists(_.id == id) && !changeExistingAccount)) {
      signup
    } else {
      val u = User(id, name, region, pass, col, db)
      if(changeExistingAccount) 
        update(u, User.withId(col, db, id))
      db.putXml(u.toXml, col + "/users", id + ".xml")
      Action(user(id))
    }
  }
  
  def update(u:User,old:User) = {
    u.upvotes = old.upvotes
    u.upvoted = old.upvoted
    u.downvotes = old.downvotes
    u.downvoted = old.downvoted
    u.edits = old.edits
    u.latestPage = old.latestPage
    u.latestWord = old.latestWord
  }
  
  def login = html.login()
  
  def loginCheck() = {
    val id = params.get("id")
    val pass = params.get("pass")
    val message = get("views.signup.error")
    Validation.required("id", id).message(message)
    Validation.required("pass", pass).message(message)
    loginWith(id, pass)
  }
  
  def loginWith(@Required id: String, @Required pass: String) = {
    println("login; id: %s, pass: %s".format(id, pass))
    val users = loadUsers
    if (Validation.hasErrors || User.withId(col, db, id) == null || User.withId(col, db, id).pass != pass) {
      Action(login) // TODO: detailed error message
    } else {
      session.put("username", id)
      Action(user(id))
    }
  }
  
  def logout = {
    session.put("username", null)
    Redirect(Http.Request.current().headers.get("referer").value)
  }

  def changeLanguage(lang: String) = {
    response.setHeader("P3P", "CP=\"CAO PSA OUR\"")
    Lang.change(lang)
    val refererURL = Http.Request.current().headers.get("referer").value
    Redirect(refererURL)
  }

  def find() = {
    val term = params.get("term")
    val volume = params.get("volume")
    search(term, volume)
  }
  
  def search(@Required term: String, @Required volume: String) = {
    val volumes = Index.RF
    val vol = if (volume.toInt - 1 >= 0) "PPN345572629_" + volumes(volume.toInt - 1) else ""
    val query = createQuery("/page", term)
    val q = db.query("drc-plain/" + vol, configure(query))
    val rows = (q \ "tr")
    val links = rows.map((n: Node) => imageLink((n \\ "a" \ "@href").text))
    val pages:Seq[Elem] = for ((row, link) <- rows zip links) yield {
      val file = link.split("/").last.split("_").last.split("-")
      val (volume, page) = (file.head, file.last.split("\\.").head)
      val text = textLink(link)
      <tr>
        {
          <td>{ Index.Volumes(volume.toInt) }</td> ++
            withLink((row \ "td").toString) ++
            //<td>{new MetsTransformer("PPN345572629_" + volume + ".xml", db).label(page.toInt)}</td> ++ // TODO: cache
            <td><a href={ link }>image</a></td> ++
            <td><a href={ text }>text</a></td>
        }
      </tr>
    }
    val label = if (volume.toInt - 1 >= 0) Index.Volumes(volumes(volume.toInt - 1).toInt) else ""
    html.search(term, label, pages, volume)
 }
  
  def withLink(elems:String) = {
    val LinkParser = """(?s).*?href="([^"]+)".*?""".r
    val LinkParser(id) = elems
    XML.loadString(<i>{ Unparsed(elems.replace(id,textLink(imageLink(id)))) }</i>.toString) \"td"
  }

  def createQuery(selector: String, term: String) = {
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
    <query xmlns="http://exist.sourceforge.net/NS/exist" start="1" max="999">
      <text>{ Unparsed(cdata) }</text>
      <properties><property name="indent" value="yes"/></properties>
    </query>
  }

}
