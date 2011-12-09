/**************************************************************************************************
 * Copyright (c) 2010 Fabian Steeg. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: Fabian Steeg - initial API and implementation
 *************************************************************************************************/
package de.uni_koeln.ub.drc.ui;

import de.uni_koeln.ub.drc.ui.facades.NLSHelper;

@SuppressWarnings("javadoc")
public class Messages {

	public String AddAnnotationTo;
	public String Value;
	public String Key;
	public String AddNewComment;
	public String AddNewTagToCurrentPage;
	public String AddTag;
	public String Author;
	public String SavePage;
	public String SavingPage;
	public String SearchingIn;
	public String SelectPagesToExport;
	public String SettingPage;
	public String SuggestionsFor;
	public String Originally;
	public String CannotVoteForOwnLong;
	public String CannotVoteForOwnShort;
	public String CanVoteOnlyOnceLong;
	public String CanVoteOnlyOnceShort;
	public String Comment;
	public String Comments;
	public String CouldNotLoadImageForCurrentPage;
	public String CouldNotLoadScan;
	public String CurrentPageModified;
	public String CurrentPageVolume;
	public String Date;
	public String Downvote;
	public String EditSuggestionsDisabled;
	public String EditSuggestionsSearchJob;
	public String Entry;
	public String Error;
	public String Export;
	public String ExportedTo;
	public String FindingEditSuggestions;
	public String For;
	public String Form;
	public String Has;
	public String Hit;
	public String Hits;
	public String In;
	public String IsLocked;
	public String LoadingData;
	public String Login;
	public String LoginFailed;
	public String LoginToDrc;
	public String Modified;
	public String NoEditSuggestionsWordIsLocked;
	public String NoEntries;
	public String NoPagesSelected;
	public String NoReasonableEditSuggestionsFound;
	public String NotTagged;
	public String NoWordSelected;
	public String Page;
	public String Pages;
	public String Password;
	public String ReloadingPage;
	public String Revert;
	public String Reverted;
	public String RevertedTo;
	public String SuggestCorrections;
	public String TaggedAs;
	public String Tags;
	public String Text;
	public String TextFiles;
	public String Upvote;
	public String User;
	public String Volume;
	public String Vote;
	public String Voted;
	public String Votes;
	public String YourRecentEdit;
	public String From;
	public String Zoom;
	public String Plus;
	public String Minus;
	public String ZoomToolTip;
	public String ClosePage;
	public String NoMeta;
	public String Show;
	public String All;
	public String Open;

	private Messages() {
		// prevent instantiation
	}

	public static Messages get() {
		return NLSHelper.getMessages();
	}
}
