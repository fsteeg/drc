/**************************************************************************************************
 * Copyright (c) 2010 Fabian Steeg. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: Fabian Steeg - initial API and implementation
 *************************************************************************************************/
package de.uni_koeln.ub.drc.util

import org.apache.commons.httpclient._
import org.apache.commons.httpclient.methods._
import scala.xml._

/* Moves issues from one GitHub repo to another. Will get all open and closed issues from a source 
 * repo, create corresponding issues in the target repo, adding labels and comments from the source 
 * issues. Requires Apache Commons httpclient, logging and codec. Set repo properties below. */

object GitHubIssuesMover {
  
  val SourceUser = "fsteeg"
  val SourceRepo = "drc"
  val TargetLogin = "fsteeg" // if not moving to org, this is the same as the target user name
  val Token = "" // API token from Account Settings > Account Admin
  val TargetUser = "spinfo"
  val TargetRepo = "drc"
    
  def main(args:Array[String]):Unit = {
    new Move().from(SourceUser, SourceRepo).to(TargetLogin, Token, TargetUser, TargetRepo)
  }
}

case class Issue(title:String, body:String, labels:Seq[String], comments:Seq[String], state:State.Value)

object State extends Enumeration { val Open, Closed = Value }

class Move {
  
  private val Http = new HttpClient
  private val IssuesApi = "http://github.com/api/v2/xml/issues/"
  private val ListApi = "list/%s/%s/%s" // user, repo, state
  private val CommentListApi = "comments/%s/%s/%s" // user, repo, id
  private val CommentAddApi = "comment/%s/%s/%s" // user, repo, id
  private val OpenApi = "open/%s/%s" // user, repo
  private val CloseApi = "close/%s/%s/%s" // user, repo, id
  private val LabelApi = "label/add/%s/%s/%s/%s" // user, repo, label, id
    
  private var issues:Seq[Issue] = Nil
    
  def from(user:String, repo:String):Move = {
    printf("Getting issues from %s/%s...\n", user, repo)
    issues = getIssues(user, repo, State.Closed) ++ getIssues(user, repo, State.Open)
    printf("Got %s issues from %s/%s...\n", issues.size, user, repo)
    this
  }
  
  def to(login:String, token:String, target:String, repo:String) = {
    printf("Adding %s issues to %s/%s...\n", issues.size, target, repo)
    for(issue <- issues) {
      val id = addIssue(issue, login, token, target, repo)
      addLabels(id, issue, login, token, target, repo)
      addComments(id, issue, login, token, target, repo)
    }
    printf("Added %s issues to %s/%s\n", issues.size, target, repo)
  }
  
  private def addIssue(issue:Issue, login:String, token:String, target:String, repo:String) = {
    val res = post(IssuesApi + OpenApi.format(target, repo), 
        "login" -> login, "token" -> token, "title" -> issue.title, "body" -> issue.body)
    val id = (res \\"number").text
    if(issue.state == State.Closed) {
      post(IssuesApi + CloseApi.format(target, repo, id), 
          "login" -> login, "token" -> token)
    }
    id
  }
  
  private def addLabels(id:String, issue:Issue, login:String, token:String, target:String, repo:String) = {
    for(label <- issue.labels) {
      post(IssuesApi + LabelApi.format(target, repo, label, id), 
          "login" -> login, "token" -> token)
    }
  }
  
  private def addComments(id:String, issue:Issue, login:String, token:String, target:String, repo:String) = {
    for(comment <- issue.comments) {
      post(IssuesApi + CommentAddApi.format(target, repo, id), 
          "login" -> login, "token" -> token, "comment" -> comment)
    }
  }
  
  private def getBody(x:Node, originalUser:String, originalRepo:String, originalId:String) = {
    val author = (x \ "user").text
    val originalDate = (x \ "updated-at").text.split("T")(0)
    /* Since we recreate new issues in the target repo, they have the authenticated user as their
     * author and the current date - to maintain the original information we add it to the issue and
     * comment bodies and add a reference to the original user and issue: */
    ((x \ "body").text + 
        "\n\n*Original author: [%s](http://github.com/%s), %s, at %s/%s#%s*".format( 
        author, author, originalDate, originalUser, originalRepo, originalId)).trim
  }

  private def getIssues(user:String, repo:String, state:State.Value):Seq[Issue] = {
    val response = get(IssuesApi + ListApi.format(user, repo, state.toString.toLowerCase))
    for(issue <- response \ "issue") yield {
      val labels = (issue \\ "label" \ "name").map(_.text)
      val body = getBody(issue, user, repo, (issue\"number").text)
      Issue((issue\"title").text,  body, labels, getComments(issue, user, repo), state)
    }
  }
  
  private def getComments(issue:Node, user:String, repo:String):Seq[String] = {
    if((issue \ "comments").text != "0") {
      val comments = get(IssuesApi + CommentListApi.format(user, repo, (issue \ "number").text))
      (comments \ "comment").map( c => getBody(c, user, repo, (issue \ "number").text))
    } else Nil
  }
  
  private def post(url:String, params:(String,String)*) = {
    val method = new PostMethod(url)
    method.setRequestBody(params.map(p => new NameValuePair(p._1, p._2)).toArray)
    execute(method)
  }
  
  private def get(url:String) = execute(new GetMethod(url))
  
  private def execute(m:HttpMethodBase) = {
    try { Http.executeMethod(m) } catch { case e => e.printStackTrace() }
    println("Executed: " + m.getURI)
    Thread.sleep(1100) // GitHub API allows 60 requests per minute, so we sleep for about 1 second
    XML.loadString(new String(m.getResponseBody))
  }
  
}
