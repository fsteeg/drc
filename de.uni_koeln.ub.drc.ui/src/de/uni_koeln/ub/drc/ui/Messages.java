/**************************************************************************************************
 * Copyright (c) 2011 Fabian Steeg. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: Fabian Steeg - initial API and implementation
 *************************************************************************************************/
package de.uni_koeln.ub.drc.ui;

import org.eclipse.osgi.util.NLS;

@SuppressWarnings("javadoc")
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "plugin"; //$NON-NLS-1$
	public static String AddAnnotationTo;
	public static String Value;
	public static String Key;
	public static String AddNewComment;
	public static String AddNewTagToCurrentPage;
	public static String AddTag;
	public static String Author;
	public static String SavePage;
	public static String SavingPage;
	public static String SearchingIn;
	public static String SelectPagesToExport;
	public static String SettingPage;
	public static String SuggestionsFor;
	public static String Originally;
	public static String CannotVoteForOwnLong;
	public static String CannotVoteForOwnShort;
	public static String CanVoteOnlyOnceLong;
	public static String CanVoteOnlyOnceShort;
	public static String Comment;
	public static String Comments;
	public static String CouldNotLoadImageForCurrentPage;
	public static String CouldNotLoadScan;
	public static String CurrentPageModified;
	public static String CurrentPageVolume;
	public static String Date;
	public static String Downvote;
	public static String EditSuggestionsDisabled;
	public static String EditSuggestionsSearchJob;
	public static String Entry;
	public static String Error;
	public static String Export;
	public static String ExportedTo;
	public static String FindingEditSuggestions;
	public static String For;
	public static String Form;
	public static String Has;
	public static String Hit;
	public static String Hits;
	public static String In;
	public static String IsLocked;
	public static String LoadingData;
	public static String Login;
	public static String LoginFailed;
	public static String LoginToDrc;
	public static String Modified;
	public static String NoEditSuggestionsWordIsLocked;
	public static String NoEntries;
	public static String NoPagesSelected;
	public static String NoReasonableEditSuggestionsFound;
	public static String NotTagged;
	public static String NoWordSelected;
	public static String Page;
	public static String Pages;
	public static String Password;
	public static String ReloadingPage;
	public static String Revert;
	public static String Reverted;
	public static String RevertedTo;
	public static String SuggestCorrections;
	public static String TaggedAs;
	public static String Tags;
	public static String Text;
	public static String TextFiles;
	public static String Upvote;
	public static String User;
	public static String Volume;
	public static String Vote;
	public static String Voted;
	public static String Votes;
	public static String YourRecentEdit;
	public static String From;
	public static String Zoom;
	public static String Plus;
	public static String Minus;
	public static String ZoomToolTip;
	public static String ClosePage;
	public static String NoMeta;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
