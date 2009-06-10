package com.artcom.y60.gom;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.net.Uri;
import android.os.RemoteException;

import com.artcom.y60.Constants;
import com.artcom.y60.HttpHelper;
import com.artcom.y60.Logger;

/**
 * Representation of the state of a node resource in the GOM. Instances may (and
 * will) load data lazily, i.e. fetching nodes/attributes may trigger reading
 * new data from the GOM repository.
 * 
 * This implementation is whiny regarding clients trying to fetch non existing
 * entries, i.e. throws a NoSuchElementException.
 * 
 * Getter methods for collections (entries/attributes/nodes) always return
 * copies of internal collections.
 * 
 * @author arne
 */
public class GomNode extends GomEntry {

    // Constants ---------------------------------------------------------

    private static final String LOG_TAG = "GomNode";

    // Static Methods ----------------------------------------------------

    public static String extractNameFromPath(String pPath) {

        return pPath.substring(pPath.lastIndexOf("/") + 1);
    }

    // Instance Variables ------------------------------------------------

    /** All entries of this node */
    private Map<String, GomEntry> mEntries;

    // Constructors ------------------------------------------------------

    protected GomNode(String pPath, GomProxyHelper pHelper) {

        super(extractNameFromPath(pPath), pPath, pHelper);

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

        for (GomEntry entry : mEntries.values()) {

            if (entry instanceof GomAttribute) {

                attrs.add((GomAttribute) entry);
            }
        }

        return attrs;
    }

    public Set<GomNode> nodes() {

        loadDataIfNecessary();

        Set<GomNode> nodes = new HashSet<GomNode>();

        for (GomEntry entry : mEntries.values()) {

            if (entry instanceof GomNode) {

                nodes.add((GomNode) entry);
            }
        }

        return nodes;
    }

    public Set<String> entryNames() {

        loadDataIfNecessary();

        Set<String> names = new HashSet<String>();
        names.addAll(mEntries.keySet());

        return names;
    }

    public GomEntry getEntry(String pName) throws NoSuchElementException {

        loadDataIfNecessary();

        GomEntry entry = mEntries.get(pName);

        if (entry == null) {

            throw new NoSuchElementException("There is no entry for '" + pName + "' in node '"
                    + getPath() + "'!");
        }

        return entry;
    }

    private boolean hasEntry(String pName) {
        loadDataIfNecessary();
        return mEntries.containsKey(pName);
    }

    public void deleteAttribute(String pAttrName) {
        HttpHelper.delete(Uri.parse(getUri() + ":" + pAttrName));
    }

    public GomAttribute getAttribute(String pName) throws NoSuchElementException,
            GomEntryTypeMismatchException {

        loadDataIfNecessary();

        GomEntry entry = getEntry(pName); // throws no such element if nonexist

        return entry.forceAttributeOrException();
    }

    
    public boolean hasAttribute(String pName) {
        return hasEntry(pName);
    }

    public GomAttribute getOrCreateAttribute(String pName) {

        GomAttribute attribute;
        try {
            attribute = getAttribute(pName);
        } catch (NoSuchElementException e) {
            GomHttpWrapper.updateOrCreateAttribute(Uri.parse(getUri() + ":" + pName), "");
            Logger.e(LOG_TAG, "creating nonexistend gom attribute");
            try {
                refresh();
            } catch (RemoteException e1) {
                Logger.e(LOG_TAG, "could not refresh gom node " + this);
            }
            attribute = getAttribute(pName);
        }

        return attribute;
    }

    public GomNode getNode(String pName) throws NoSuchElementException,
            GomEntryTypeMismatchException {

        loadDataIfNecessary();

        GomEntry entry = getEntry(pName); // throws no such element if nonexist

        return entry.forceNodeOrException();
    }

    public void refresh() throws RemoteException {
        getProxy().refreshEntry(getPath());
        loadData();
    }

    public JSONObject toJson() {

        return toJsonFlushEntries(true);
    }

    // Private Instance Methods ------------------------------------------

    private JSONObject toJsonFlushEntries(boolean pFlush) {

        // { "node": {
        // "uri": <uri>,
        // "entries": [
        // <children>
        // ]
        // } }

        try {

            JSONObject json = new JSONObject();

            JSONObject node = new JSONObject();
            json.put(Constants.Gom.Keywords.NODE, node);

            node.put(Constants.Gom.Keywords.URI, getPath());

            if (pFlush) {

                loadDataIfNecessary();
            }

            if (isDataLoaded()) {

                JSONArray entries = new JSONArray();
                for (GomEntry entry : mEntries.values()) {

                    if (entry instanceof GomNode) {

                        entries.put(((GomNode) entry).toJsonFlushEntries(false));

                    } else {

                        entries.put(entry.toJson());
                    }

                }
            }

            return json;

        } catch (JSONException e) {

            throw new RuntimeException(e);
        }
    }

    private boolean isDataLoaded() {

        return (mEntries != null);
    }

    private void loadDataIfNecessary() {

        if (!isDataLoaded()) {

            loadData();
        }
    }

    private void loadData() {
        try {
            mEntries = new HashMap<String, GomEntry>();

            List<String> subNodeNames = new LinkedList<String>();
            List<String> attributeNames = new LinkedList<String>();
            String path = getPath();

            getProxy().getNodeData(path, subNodeNames, attributeNames);

            GomProxyHelper helper = getGomProxyHelper();

            for (String name : attributeNames) {

                GomAttribute attr = helper.getAttribute(path + ":" + name);
                mEntries.put(name, attr);
            }

            for (String name : subNodeNames) {

                GomNode node = helper.getNode(path + "/" + name);
                mEntries.put(name, node);
            }

        } catch (RemoteException rex) {

            Logger.e(LOG_TAG, "failed to retrieve node data", rex);
            throw new RuntimeException(rex);
        }
    }

}
