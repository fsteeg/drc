/**
 * ************************************************************************************************
 * Copyright (c) 2011 Fabian Steeg. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: Fabian Steeg - initial API and implementation
 * ***********************************************************************************************
 */
package controllers

import play._
import play.mvc._

trait Secure {
  self: Controller =>
  @Before(only = Array("edit")) def checkSecurity = {
    session("username") match {
      case Some(username) => Continue
      case None => Action(Application.login)
    }
  }
  @Util def userName = session.get("username")
} 
