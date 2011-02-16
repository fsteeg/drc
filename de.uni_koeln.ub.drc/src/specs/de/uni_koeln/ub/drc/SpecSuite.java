/**************************************************************************************************
 * Copyright (c) 2010 Fabian Steeg. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: Fabian Steeg - initial API and implementation
 *************************************************************************************************/

package de.uni_koeln.ub.drc;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import de.uni_koeln.ub.drc.data.SpecDrcDb;
import de.uni_koeln.ub.drc.data.SpecIndex;
import de.uni_koeln.ub.drc.data.SpecModification;
import de.uni_koeln.ub.drc.data.SpecPage;
import de.uni_koeln.ub.drc.data.SpecUser;
import de.uni_koeln.ub.drc.data.SpecWord;
import de.uni_koeln.ub.drc.util.SpecMetsTransformer;

/**
 * Main test suite.
 * @author Fabian Steeg (fsteeg)
 */
@RunWith( Suite.class )
@Suite.SuiteClasses( { SpecWord.class, SpecPage.class, SpecIndex.class,
        SpecUser.class, SpecModification.class, SpecDrcDb.class, SpecMetsTransformer.class } )
public final class SpecSuite {}
