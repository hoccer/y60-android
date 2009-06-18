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

    public Set<GomEntry> entries() throws GomEntryTypeMismatchException, GomNotFoundException {

        loadDataIfNecessary();

        Set<GomEntry> entries = new HashSet<GomEntry>();
        entries.addAll(mEntries.values());

        return entries;
    }

    public Set<GomAttribute> attributes() throws GomEntryTypeMismatchException,
            GomNotFoundException {

        loadDataIfNecessary();

        Set<GomAttribute> attrs = new HashSet<GomAttribute>();

        for (GomEntry entry : mEntries.values()) {

            if (entry instanceof GomAttribute) {

                attrs.add((GomAttribute) entry);
            }
        }

        return attrs;
    }

    public Set<GomNode> nodes() throws GomEntryTypeMismatchException, GomNotFoundException {

        loadDataIfNecessary();

        Set<GomNode> nodes = new HashSet<GomNode>();

        for (GomEntry entry : mEntries.values()) {

            if (entry instanceof GomNode) {

                nodes.add((GomNode) entry);
            }
        }

        return nodes;
    }

    public Set<String> entryNames() throws GomEntryTypeMismatchException, GomNotFoundException {

        loadDataIfNecessary();

        Set<String> names = new HashSet<String>();
        names.addAll(mEntries.keySet());

        return names;
    }

    public GomEntry getEntry(String pName) throws NoSuchElementException,
            GomEntryTypeMismatchException, GomNotFoundException {

        loadDataIfNecessary();

        GomEntry entry = mEntries.get(pName);

        if (entry == null) {

            throw new NoSuchElementException("There is no entry for '" + pName + "' in node '"
                    + getPath() + "'!");
        }

        return entry;
    }

    private boolean hasEntry(String pName) throws GomEntryTypeMismatchException,
            GomNotFoundException {
        loadDataIfNecessary();
        return mEntries.containsKey(pName);
    }

    public void deleteAttribute(String pAttrName) {
        HttpHelper.delete(Uri.parse(getUri() + ":" + pAttrName));
    }

    public GomAttribute getAttribute(String pName) throws NoSuchElementException,
            GomEntryTypeMismatchException, GomNotFoundException {

        loadDataIfNecessary();

        GomEntry entry = getEntry(pName); // throws no such element if nonexist

        return entry.forceAttributeOrException();
    }

    public boolean hasAttribute(String pName) throws GomEntryTypeMismatchException,
            GomNotFoundException {
        return hasEntry(pName);
    }

    public GomAttribute getOrCreateAttribute(String pName) throws GomEntryTypeMismatchException,
            GomNotFoundException {

        GomAttribute attribute;
        try {
            attribute = getAttribute(pName);
        } catch (NoSuchElementException e) {
            GomHttpWrapper.updateOrCreateAttribute(Uri.parse(getUri() + ":" + pName), "");
            Logger.e(LOG_TAG, "creating nonexistend gom attribute");

            attribute = getAttribute(pName);
        }

        return attribute;
    }

    public GomNode getNode(String pName) throws NoSuchElementException,
            GomEntryTypeMismatchException, GomNotFoundException {

        loadDataIfNecessary();

        GomEntry entry = getEntry(pName); // throws no such element if nonexist

        return entry.forceNodeOrException();
    }

    public JSONObject toJson() throws GomEntryTypeMismatchException, GomNotFoundException {

        return toJsonFlushEntries(true);
    }

    public boolean equals(Object pObject) {

        if (pObject != null && pObject instanceof GomNode && super.equals(pObject)) {

            GomNode other = (GomNode) pObject;
            if (!isDataLoaded() || !other.isDataLoaded()) {

                throw new IllegalStateException(
                        "Trying to call equals on a GomNode which has not been loaded lazily!");
            }

            return mEntries.keySet().equals(other.mEntries.keySet());

        } else {

            return false;
        }
    }

    // Private Instance Methods ------------------------------------------

    private JSONObject toJsonFlushEntries(boolean pFlush) throws GomEntryTypeMismatchException,
            GomNotFoundException {

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

    private void loadDataIfNecessary() throws GomEntryTypeMismatchException, GomNotFoundException {

        if (!isDataLoaded()) {

            loadData();
        }
    }

    private void loadData() throws GomEntryTypeMismatchException, GomNotFoundException {
        mEntries = new HashMap<String, GomEntry>();

        List<String> subNodeNames = new LinkedList<String>();
        List<String> attributeNames = new LinkedList<String>();
        String path = getPath();

        GomProxyHelper helper = getGomProxyHelper();
        helper.getNodeData(path, subNodeNames, attributeNames);

        for (String name : attributeNames) {

            GomAttribute attr = helper.getAttribute(path + ":" + name);
            mEntries.put(name, attr);
        }

        for (String name : subNodeNames) {

            GomNode node = helper.getNode(path + "/" + name);
            mEntries.put(name, node);
        }
    }

}
