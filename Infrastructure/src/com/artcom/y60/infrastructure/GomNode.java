package com.artcom.y60.infrastructure;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Representation of a node in the GOM. Instances may (and will) load data lazily, i.e.
 * fetching nodes/attributes may trigger reading new data from the GOM repository!
 * 
 * This implementation is whiny regarding clients trying to fetch non existing entries,
 * i.e. throws a NoSuchElementException
 * 
 * Getter methods for collections (entries/attributes/nodes) always return copies of internal
 * collections.
 * 
 * @author arne
 */
public class GomNode extends GomEntry {
    
    // Instance Variables ------------------------------------------------

    private Map<String, GomEntry> mEntries;

    
    
    // Static Methods ----------------------------------------------------

    static GomNode fromJson(JSONObject pJson, GomRepository pRepos) throws JSONException {
        
        Object tmpNode = pJson.opt(GomKeywords.NODE);
        
        if (tmpNode instanceof String) {
            
            // it's just a reference to a node
            String path = (String)tmpNode;
            String name = path.substring(path.lastIndexOf("/")+1);
            
            return new GomNode(name, path, pRepos);
            
        } else if (tmpNode instanceof JSONObject) {
            
            JSONObject jsNode = (JSONObject)tmpNode;
            String     path   = jsNode.getString(GomKeywords.URI);
            String     name   = path.substring(path.lastIndexOf("/")+1);
            
            GomNode node = new GomNode(name, path, pRepos);
            node.fillDataFromJson(jsNode);
            
            return node;
            
        } else {
            
            throw new IllegalArgumentException("Unrecognized JSON structure for reading as GOM node: "+pJson.toString());
        }
    }
    
    

    // Constructors ------------------------------------------------------

    GomNode(String pName, String pPath, GomRepository pRepos) {
        
        super(pName, pPath, pRepos);
        
        mEntries = null; // will be loaded lazily
    }
    
    
    
    // Public Instance Methods -------------------------------------------

    public Set<GomEntry> entries() {
        
        loadDataIfNecessary();
        
        Set<GomEntry> entries = new HashSet<GomEntry>();
        entries.addAll(mEntries.values());
        
        return entries;
    }
    
    
    public Set<GomAttribute> attributes() {
        
        loadDataIfNecessary();
        
        Set<GomAttribute> attrs = new HashSet<GomAttribute>();
        
        for (GomEntry entry: mEntries.values()) {
            
            if (entry instanceof GomAttribute) {
                
                attrs.add((GomAttribute)entry);
            }
        }
        
        return attrs;
    }
    
    
    public Set<GomNode> nodes() {
        
        loadDataIfNecessary();
        
        Set<GomNode> nodes = new HashSet<GomNode>();
        
        for (GomEntry entry: mEntries.values()) {
            
            if (entry instanceof GomNode) {
                
                nodes.add((GomNode)entry);
            }
        }
        
        return nodes;
    }
    
    
    public Set<String> keys() {
        
        loadDataIfNecessary();
        
        Set<String> keys = new HashSet<String>();
        keys.addAll(mEntries.keySet());

        return keys;
    }
    
    
    public GomEntry getEntry(String pKey) throws NoSuchElementException {
        
        loadDataIfNecessary();
        
        GomEntry entry = mEntries.get(pKey);
        
        if (entry == null) {
            
            throw new NoSuchElementException("There is no entry for '"+pKey+"' in node '"+getPath()+"'!");
        }
        
        return entry;
    }
    
    
    public GomAttribute getAttribute(String pKey) throws NoSuchElementException, GomEntryTypeMismatchException {
     
        loadDataIfNecessary();
        
        GomEntry entry = getEntry(pKey); // throws no such element if nonexist
        
        return entry.forceAttributeOrException();
    }
    
    
    public GomNode getNode(String pKey) throws NoSuchElementException, GomEntryTypeMismatchException {
        
        loadDataIfNecessary();
  
        GomEntry entry = getEntry(pKey); // throws no such element if nonexist
        
        return entry.forceNodeOrException();
    }
    
    
    
    // Private Instance Methods ------------------------------------------

    private void loadDataIfNecessary() {
        
        if (mEntries == null) {
            
            mEntries = new HashMap<String, GomEntry>();
            
            try {
                
                JSONObject jsob = HTTPHelper.getJson(getUri().toString());
                JSONObject node = JsonHelper.getMemberOrSelf(jsob, GomKeywords.NODE);
                fillDataFromJson(node);
                
            } catch (JSONException e) {
                
                // this may happen while lazily loading data
                // wrap as runtime ex to hide exception from interface
                throw new RuntimeException(e);
            }
        }
    }
    
    
    private void fillDataFromJson(JSONObject pJsob) throws JSONException {
        
        mEntries = new HashMap<String, GomEntry>();
        
        GomRepository repos   = getRepository();
        JSONArray     entries = pJsob.getJSONArray(GomKeywords.ENTRIES);
        
        for (int i=0; i<entries.length(); i++) {
            
            JSONObject jEntry = entries.getJSONObject(i);
            GomEntry   entry  = GomEntry.fromJson(jEntry, repos);
            mEntries.put(entry.getName(), entry);
        }
    }
}
