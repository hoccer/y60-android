package com.artcom.y60.infrastructure;

import java.util.Vector;

import android.content.UriMatcher;
import android.net.Uri;

public class GOMClient
{
	private static final int URI_TYPE_NODE  = 1;
	private static final int URI_TYPE_ENTRY = 2;
	
	private static final UriMatcher uriMatcher;
	
	static {
		uriMatcher = new UriMatcher( UriMatcher.NO_MATCH );
		uriMatcher.addURI( "com.artcom.y60.infrastructure.gom", "/", URI_TYPE_NODE );
		uriMatcher.addURI( "com.artcom.y60.infrastructure.gom", "/*:*", URI_TYPE_ENTRY );
	}
	
	public GOMClient()
	{
		
	}
	
	// Functions to take apart nodes and entries
	
	// Reads the entries under the given node
	//
	// @param node URI of the node 
	public Vector<GOMEntry> entries( Uri nodeUri )
	{
		Vector<GOMEntry> entries = new Vector<GOMEntry>();
		
		return entries;
	}
	
	public GOMNode entry( Uri entryUri )
	{
		GOMNode entry = new GOMNode();
		
		return entry
		;
	}
}