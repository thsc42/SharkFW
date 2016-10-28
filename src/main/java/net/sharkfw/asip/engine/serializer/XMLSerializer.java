package net.sharkfw.asip.engine.serializer;

import net.sharkfw.knowledgeBase.*;
import net.sharkfw.knowledgeBase.inmemory.InMemoGenericTagStorage;
import net.sharkfw.knowledgeBase.inmemory.InMemoSTSet;
import net.sharkfw.knowledgeBase.inmemory.InMemoSemanticNet;
import net.sharkfw.system.TimeLong;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;


/**
 * @author thsc
 */
public class XMLSerializer {

    private static final String SHARKCS_TAG = "cs";

    private static final String TOPICS_TAG = "topics";
    private static final String ORIGINATOR_TAG = "originator";
    private static final String PEERS_TAG = "peer";
    private static final String REMOTE_PEERS_TAG = "remotePeer";
    private static final String LOCATIONS_TAG = "location";
    private static final String TIMES_TAG = "times";
    private static final String DIRECTION_TAG = "direction";

    private static final String TAGS_ENUM_TAG = "tags";
    private static final String STSET_TAG = "stset";

    private static final String PREDICATES_TAG = "predicates";
    private static final String SUB_SUPER_TAG = "subs";
    private static final String PREDICATE_TAG = "pred";
    private static final String SUPER_TAG = "super";
    private static final String SOURCE_TAG = "source";
    private static final String TARGET_TAG = "target";

    private static final String TIME_FROM = "from";
    private static final String TIME_DURATION = "duration";

    private final String TAG_TAG = "tag";
    private final String INDEX_TAG = "index";
    private final String NAME_TAG = "name";
    private final String SI_TAG = "si";
    private final String ADDRESS_TAG = "addr";

    private final String PROPERTIES_TAG = "props";
    private final String PROPERTY_TAG = "p";
    private final String VALUE_TAG = "v";

    private String startTag(String tag) {
        return "<" + tag + ">";
    }

    private String endTag(String tag) {
        return "</" + tag + ">";
    }

    private String emptyTag(String tag) {
        return "<" + tag + "/>";
    }


    /**
     * Serializes an st set. Checks type of st set and adds relations of
     * taxonomy or semantic net is present.
     *
     * @param stset
     * @return
     * @throws SharkKBException
     */
    public String serializeSTSet(STSet stset) throws SharkKBException {
        if (stset == null) {
            return null;
        }

        // enum tags
        Enumeration<SemanticTag> tags = stset.tags();
        if (tags == null) {
            return null;
        }

        StringBuilder buf = new StringBuilder();

        buf.append(this.startTag(STSET_TAG));
        buf.append(this.startTag(TAGS_ENUM_TAG));

        // add tags
        while (tags.hasMoreElements()) {
            buf.append(this.serializeTag(tags.nextElement()));
        }

        buf.append(this.endTag(TAGS_ENUM_TAG));

        // add relations if any
        Enumeration<SemanticTag> tagEnum = stset.tags();
        if (stset instanceof SemanticNet || stset instanceof Taxonomy) {
            String serializedRelations = this.serializeRelations(tagEnum);
            if (serializedRelations != null) {
                buf.append(serializedRelations);
            }
        }

        buf.append(this.endTag(STSET_TAG));

        return buf.toString();
    }

    private String serializeRelations(Enumeration<SemanticTag> tagEnum) {

        if (tagEnum == null) {
            return null;
        }
        if (!tagEnum.hasMoreElements()) {
            return null;
        }

        SemanticTag tag = tagEnum.nextElement();

        boolean semanticNet;
        if (tag instanceof SNSemanticTag) {
            semanticNet = true;
        } else {
            if (tag instanceof TXSemanticTag) {
                semanticNet = false;
            } else {
                // no semantic net no taxonomy...
                return null;
            }
        }

        StringBuilder buf = new StringBuilder();

        boolean openTagWritten = false;

        if (semanticNet) {
            // buf.append(this.startTag(PREDICATES_TAG));
        } else {
            // buf.append(this.startTag(SUB_SUPER_TAG));
        }


        if (semanticNet) {

            // Semantic Net
            do {
                SNSemanticTag snTag = (SNSemanticTag) tag;

                // get tag for next round
                tag = null;
                if (tagEnum.hasMoreElements()) {
                    tag = tagEnum.nextElement();
                }

                String[] sSIs = snTag.getSI();
                if (sSIs != null) {
                    String sourceSI = sSIs[0];

                    Enumeration<String> pNameEnum = snTag.predicateNames();
                    if (pNameEnum != null) {
                        while (pNameEnum.hasMoreElements()) {

                            String predicateName = pNameEnum.nextElement();

                            Enumeration<SNSemanticTag> targetEnum =
                                    snTag.targetTags(predicateName);

                            if (targetEnum == null) {
                                continue;
                            }

                            while (targetEnum.hasMoreElements()) {
                                // going to write a predicate - open the whole predicate section if necessary
                                if (!openTagWritten) {
                                    openTagWritten = true;
                                    buf.append(this.startTag(PREDICATES_TAG));
                                }

                                SNSemanticTag target = targetEnum.nextElement();
                                String[] tSIs = target.getSI();
                                if (tSIs == null) {
                                    continue;
                                }

                                String targetSI = tSIs[0];

                                // write predicate
                                buf.append(this.startTag(PREDICATE_TAG));

                                // name
                                buf.append(this.startTag(NAME_TAG));
                                buf.append(predicateName);
                                buf.append(this.endTag(NAME_TAG));

                                // source
                                buf.append(this.startTag(SOURCE_TAG));
                                buf.append(this.startTag(SI_TAG));
                                buf.append(sourceSI);
                                buf.append(this.endTag(SI_TAG));
                                buf.append(this.endTag(SOURCE_TAG));

                                // target
                                buf.append(this.startTag(TARGET_TAG));
                                buf.append(this.startTag(SI_TAG));
                                buf.append(targetSI);
                                buf.append(this.endTag(SI_TAG));
                                buf.append(this.endTag(TARGET_TAG));

                                // end 
                                buf.append(this.endTag(PREDICATE_TAG));
                            }
                        }
                    }
                }
            } while (tag != null);

        } else {
            // Taxonomy
            do {
                TXSemanticTag txTag = (TXSemanticTag) tag;
                // get tag for next round
                tag = null;
                if (tagEnum.hasMoreElements()) {
                    tag = tagEnum.nextElement();
                }

                String[] sSIs = txTag.getSI();
                if (sSIs != null) {
                    String sourceSI = sSIs[0];

                    TXSemanticTag superTag = txTag.getSuperTag();
                    if (superTag != null) {
                        String[] tSIs = superTag.getSI();
                        if (tSIs == null) {
                            continue;
                        }

                        String targetSI = tSIs[0];

                        // open this relations section
                        if (!openTagWritten) {
                            openTagWritten = true;
                            buf.append(this.startTag(SUB_SUPER_TAG));
                        }

                        // write predicate
                        buf.append(this.startTag(SUPER_TAG));

                        // source
                        buf.append(this.startTag(SOURCE_TAG));
                        buf.append(this.startTag(SI_TAG));
                        buf.append(sourceSI);
                        buf.append(this.endTag(SI_TAG));
                        buf.append(this.endTag(SOURCE_TAG));

                        // target
                        buf.append(this.startTag(TARGET_TAG));
                        buf.append(this.startTag(SI_TAG));
                        buf.append(targetSI);
                        buf.append(this.endTag(SI_TAG));
                        buf.append(this.endTag(TARGET_TAG));

                        // end 
                        buf.append(this.endTag(SUPER_TAG));
                    }
                }
            } while (tagEnum.hasMoreElements());
        }

        if (openTagWritten) {
            if (semanticNet) {
                buf.append(this.endTag(PREDICATES_TAG));
            } else {
                buf.append(this.endTag(SUB_SUPER_TAG));
            }
        }

        if (buf.length() > 0) {
            return buf.toString();
        } else {
            return null;
        }
    }

    private String serializeTag(SemanticTag tag) throws SharkKBException {
        if (tag == null) {
            return null;
        }

        StringBuilder buf = new StringBuilder();

        buf.append(this.startTag(TAG_TAG));

        String name = tag.getName();
        if (name != null) {
            buf.append(this.startTag(NAME_TAG));
            buf.append(name);
            buf.append(this.endTag(NAME_TAG));
        }

        String[] sis = tag.getSI();
        if (sis != null) {
            for (int i = 0; i < sis.length; i++) {
                buf.append(this.startTag(SI_TAG));
                buf.append(sis[i]);
                buf.append(this.endTag(SI_TAG));
            }
        }

        // pst
        if (tag instanceof PeerSemanticTag) {
            PeerSemanticTag pst = (PeerSemanticTag) tag;

            String addr[] = pst.getAddresses();
            if (addr != null) {
                for (int i = 0; i < addr.length; i++) {
                    buf.append(this.startTag(ADDRESS_TAG));
                    buf.append(addr[i]);
                    buf.append(this.endTag(ADDRESS_TAG));
                }
            }
        }

        // tst
        if (tag instanceof TimeSemanticTag) {
            TimeSemanticTag tst = (TimeSemanticTag) tag;
            buf.append(this.startTag(TIME_FROM));
            buf.append(Long.toString(tst.getFrom()));
            buf.append(this.endTag(TIME_FROM));

            buf.append(this.startTag(TIME_DURATION));
            buf.append(Long.toString(tst.getDuration()));
            buf.append(this.endTag(TIME_DURATION));
        }

        // properties
        String serializedProperties = this.serializeProperties(tag);
        if (serializedProperties != null) {
            buf.append(serializedProperties);
        }

        buf.append(this.endTag(TAG_TAG));

        return buf.toString();

    }


    /**
     * Deserializes semantic tag from string
     *
     * @param s
     */
    private SemanticTag deserializeTag(STSet targetSet, String s) throws SharkKBException {
        int index;
        String name;
        ArrayList<String> sis = new ArrayList<String>();
        ArrayList<String> addresses = new ArrayList<String>();

        name = this.stringBetween(NAME_TAG, s, 0);

        // sis
        boolean found;
        index = 0;
        do {
            found = false;
            String si = this.stringBetween(SI_TAG, s, index);
            if (si != null) {
                sis.add(si);
                index = s.indexOf(this.endTag(SI_TAG), index) + 1;
                found = true;
            }
        } while (found);

        // pst ?
        if (targetSet instanceof PeerSemanticNet
                || targetSet instanceof PeerTaxonomy
                || targetSet instanceof PeerSTSet
                ) {
            do {
                found = false;
                String addr = this.stringBetween(ADDRESS_TAG, s, index);
                if (addr != null) {
                    index = s.indexOf(this.endTag(ADDRESS_TAG), index) + 1;
                    addresses.add(addr);
                    found = true;
                }
            } while (found);
        }

        // create tag if some minimal things are found
        if (name == null && sis.isEmpty()) {
            return null;
        }

        SemanticTag target;
        if (targetSet instanceof PeerSemanticNet) {
            target = ((PeerSemanticNet) targetSet).createSemanticTag(
                    name,
                    this.arrayList2Array(sis),
                    this.arrayList2Array(addresses)
            );
        } else if (targetSet instanceof PeerTaxonomy) {
            target = ((PeerTaxonomy) targetSet).createPeerTXSemanticTag(
                    name,
                    this.arrayList2Array(sis),
                    this.arrayList2Array(addresses)
            );
        } else if (targetSet instanceof PeerSTSet) {
            target = ((PeerSTSet) targetSet).createPeerSemanticTag(
                    name,
                    this.arrayList2Array(sis),
                    this.arrayList2Array(addresses)
            );
        } else {
            target = targetSet.createSemanticTag(name, this.arrayList2Array(sis));
        }

        // properties
        this.deserializeProperties(target, s);

        return target;
    }

    private String[] arrayList2Array(ArrayList<String> source) {
        if (source.isEmpty()) return null;

        String[] ret = new String[source.size()];

        Iterator<String> sIter = source.iterator();
        int i = 0;
        while (sIter.hasNext()) {
            ret[i++] = sIter.next();
        }

        return ret;
    }

    private String serializeProperties(SystemPropertyHolder target) throws SharkKBException {
        if (target == null) {
            return null;
        }

        Enumeration<String> propNamesEnum = target.propertyNames(false);
        if (propNamesEnum == null || !propNamesEnum.hasMoreElements()) {
            return this.emptyTag(PROPERTIES_TAG);
        }

        StringBuilder buf = new StringBuilder();

        buf.append(this.startTag(PROPERTIES_TAG));

        while (propNamesEnum.hasMoreElements()) {
            String name = propNamesEnum.nextElement();
            String value = target.getProperty(name);

            buf.append(this.startTag(PROPERTY_TAG));

            buf.append(this.startTag(NAME_TAG));
            buf.append(name);
            buf.append(this.endTag(NAME_TAG));

            buf.append(this.startTag(VALUE_TAG));

            // for safety reasons: put any value tag inside a CDATA section
            buf.append(XMLSerializer.CDATA_START_TAG);
            buf.append(value);
            buf.append(XMLSerializer.CDATA_END_TAG);

            buf.append(this.endTag(VALUE_TAG));

            buf.append(this.endTag(PROPERTY_TAG));
        }

        buf.append(this.endTag(PROPERTIES_TAG));

        return buf.toString();
    }

    private void deserializeProperties(SystemPropertyHolder target, String s) throws SharkKBException {
        if (s == null || target == null) {
            return;
        }

        if (s.equalsIgnoreCase(this.emptyTag(PROPERTIES_TAG))) {
            return;
        }

        int index = 0;

        String propsString = this.stringBetween(PROPERTIES_TAG, s, 0);
        if (propsString == null) {
            return;
        }

        boolean found;

        do {
            found = false;
            String propString = this.stringBetween(PROPERTY_TAG, propsString, index);
            if (propString != null) {

                String name = this.stringBetween(NAME_TAG, propString, 0);
                String value = this.stringBetween(VALUE_TAG, propString, 0);

                // cut off cdata section
                value = value.substring(this.cdata_start_tag_length, value.length() - this.cdata_end_tag_length);

                if (name != null) {
                    target.setProperty(name, value);
                }

                // next property
                index = propsString.indexOf(this.endTag(PROPERTY_TAG), index) + 1;
                found = true;
            }
        } while (found);
    }

    public boolean deserializeSTSet(STSet target, String serializedSTSet) throws SharkKBException {
        if (target == null || serializedSTSet == null) {
            return false;
        }

        String setString = this.stringBetween(STSET_TAG, serializedSTSet, 0);

        if (setString == null) {
            return false;
        }

        String tagsString = this.stringBetween(TAGS_ENUM_TAG, setString, 0);

        if (tagsString == null) {
            return false;
        }

        // parse tags
        boolean found;
        int index = 0;

        do {
            found = false;
            String tagString = this.stringBetween(TAG_TAG, tagsString, index);
            if (tagString != null) {
                found = true;

                this.deserializeTag(target, tagString);

                index = tagsString.indexOf(this.endTag(TAG_TAG), index) + 1;
            }
        } while (found);

        // more than a plain set ?
        SemanticNet sn = null;
        try {
            sn = this.cast2SN(target);
            this.deserializeRelations(sn, setString);
            // relations
        } catch (SharkKBException kb) {
            // just a simple set - ok, return
            return true;
        }

        return true;
    }

    private void deserializeRelations(Taxonomy target, String source) {
        String relationsString = this.stringBetween(SUB_SUPER_TAG, source, 0);

        if (relationsString == null) {
            return;
        }

        int index = 0;

        boolean found = false;
        do {
            found = false;
            String relationString = this.stringBetween(SUPER_TAG,
                    relationsString, index);

            if (relationString == null) continue;

            found = true;
            // adjust index for next try
            index = relationsString.indexOf(this.endTag(SUPER_TAG)) + 1;

            String superTagString = this.stringBetween(SOURCE_TAG, relationString, 0);
            if (superTagString == null) continue;

            String sourceSI = this.stringBetween(SI_TAG, relationString, 0);
            if (sourceSI == null) continue;

            String targetTagString = this.stringBetween(TARGET_TAG, relationString, 0);
            if (targetTagString == null) continue;

            String targetSI = this.stringBetween(SI_TAG, relationString, 0);
            if (targetSI == null) continue;

            try {
                TXSemanticTag sourceTag = (TXSemanticTag) target.getSemanticTag(sourceSI);
                if (sourceTag == null) continue;

                TXSemanticTag targetTag = (TXSemanticTag) target.getSemanticTag(targetSI);
                if (targetTag == null) continue;

                // set super tag
                sourceTag.move(targetTag);
            } catch (SharkKBException skbe) {
                // ignore and go ahead
                continue;
            }
        } while (found);
    }

    private void deserializeRelations(SemanticNet target, String source) {
        String relationsString = this.stringBetween(PREDICATES_TAG, source, 0);

        if (relationsString == null) return;

        int index = 0;

        boolean found = false;
        do {
            found = false;
            String predicateString = this.stringBetween(PREDICATE_TAG,
                    relationsString, index);

            if (predicateString == null) continue;

            found = true;
            // adjust index for next try
            index = relationsString.indexOf(this.endTag(PREDICATE_TAG), index) + 1;

            String nameString = this.stringBetween(NAME_TAG, predicateString, 0);
            if (nameString == null) continue;

            String sourceTagString = this.stringBetween(SOURCE_TAG, predicateString, 0);
            if (sourceTagString == null) continue;

            String sourceSI = this.stringBetween(SI_TAG, sourceTagString, 0);
            if (sourceSI == null) continue;

            String targetTagString = this.stringBetween(TARGET_TAG, predicateString, 0);
            if (targetTagString == null) continue;

            String targetSI = this.stringBetween(SI_TAG, targetTagString, 0);
            if (targetSI == null) continue;

            try {
                SNSemanticTag sourceTag = (SNSemanticTag) target.getSemanticTag(sourceSI);
                if (sourceTag == null) continue;

                SNSemanticTag targetTag = (SNSemanticTag) target.getSemanticTag(targetSI);
                if (targetTag == null) continue;

                sourceTag.setPredicate(nameString, targetTag);

            } catch (SharkKBException skbe) {
                // ignore and go ahead
                continue;
            }
        } while (found);
    }

    private static final String CDATA_START_TAG = "<!CDATA[";
    private static final String CDATA_END_TAG = "]]>";

    private final int cdata_start_tag_length = XMLSerializer.CDATA_START_TAG.length();
    private final int cdata_end_tag_length = XMLSerializer.CDATA_END_TAG.length();

    /**
     * Checks in source string if index is inside a cdata section. If so the first
     * index that is behind that cdata section is returned. -1 is returned otherwise.
     */
    private int endOfCDataSection(String source, int startIndex, int position) {
        int cdataStart = source.indexOf(XMLSerializer.CDATA_START_TAG, startIndex);

        // there is no cdata section 
        if (cdataStart == -1) {
            return -1;
        }

        // there is one - where does is end?
        int cdataEnd = source.indexOf(XMLSerializer.CDATA_END_TAG, cdataStart);

        if (cdataEnd == -1) {
            // malformed structure!!
            return -1;
        }
        
        /* ok there is a cdata section
         * is position inside?
        */

        // is position in before cdata section? - ok
        if (position < cdataStart) {
            return -1;
        }

        // not before cdatasection

        // inside?
        if (position > cdataStart && position < cdataEnd) {
            // inside - return end of section
            return cdataEnd + this.cdata_end_tag_length + 1;
        }

        // it's behind that cdata section - maybe there is another one
        return this.endOfCDataSection(source, cdataEnd, position);
    }

    /**
     * Return string between a given tag. Null is returned if no tag can be found
     * which is also happens with malformed formats.
     *
     * @param tag    tag to look for
     * @param index  begin search at this index in source
     * @param source tag should be found in this string
     * @return
     */
    private String stringBetween(String tag, String source, int index) {
        if (source == null || tag == null || index < 0) {
            return null;
        }

        if (index >= source.length()) {
            return null;
        }
        
        /* <![CDATA[ An in-depth look at creating applications with XML, using <, >,]]>
        *
        */

        int startIndex = 0;
        int cdataEnd;

        do {
            cdataEnd = -1;
            startIndex = source.indexOf(this.startTag(tag), index);

            if (startIndex == -1) {
                return null;
            }

            // inside cdata section?
            cdataEnd = this.endOfCDataSection(source, index, startIndex);

            if (cdataEnd > -1) {
                index = cdataEnd;
            }

            // do again until tag outside cdata section was found
        } while (cdataEnd > -1);

        int endIndex;

        index = startIndex;

        do {
            endIndex = source.indexOf(this.endTag(tag), index);

            if (endIndex == -1) {
                return null;
            }

            // inside cdata section?
            cdataEnd = this.endOfCDataSection(source, index, endIndex);

            if (cdataEnd > -1) {
                index = cdataEnd;
            }

            // do again until tag outside cdata section was found
        } while (cdataEnd > -1);

        // cut start-Tag
        startIndex += this.startTag(tag).length();

        String retString = source.substring(startIndex, endIndex);

        if (retString.length() == 0) {
            return null;
        }

        return retString;
    }


    private SemanticTag getFirstTag(STSet stSet) throws SharkKBException {
        if (stSet == null) return null;
        Enumeration<SemanticTag> tagEnum = stSet.tags();
        if (tagEnum == null) return null;

        return tagEnum.nextElement();

    }

    // TODO
    private boolean deserializeSTSet(SpatialSTSet locations, String partString) throws SharkKBException {
        // TODO - workaround until spatial and time tags finished
        return this.deserializeSTSet((STSet) locations, partString);
    }

    // TODO
    private boolean deserializeSTSet(TimeSTSet times, String partString) throws SharkKBException {

        int index = 0;
        boolean found = false;

        do {
            found = false;
            String fromString = this.stringBetween(TIME_FROM, partString, index);

            long from = TimeSemanticTag.FIRST_MILLISECOND_EVER;

            if (fromString != null) {
                found = true;
                from = TimeLong.parse(fromString);
            }

            String durationString = this.stringBetween(TIME_DURATION, partString, index);

            long duration = TimeSemanticTag.FOREVER;

            if (durationString != null) {
                found = true;
                duration = Long.parseLong(durationString);
            }

            if (found) {
                times.createTimeSemanticTag(from, duration);

                int fromIndex = partString.indexOf(this.endTag(TIME_FROM));
                int durationIndex = partString.indexOf(this.endTag(TIME_DURATION));

                index = fromIndex > durationIndex ? fromIndex : durationIndex;
                index++;
            }

        } while (found && index > 0);

        return !times.isEmpty();
    }

    private SemanticNet cast2SN(STSet stset) throws SharkKBException {
        SemanticNet sn;
        try {
            sn = (SemanticNet) stset;
        } catch (ClassCastException e) {
            InMemoSTSet imset = null;

            try {
                imset = (InMemoSTSet) stset;
            } catch (ClassCastException cce) {
                throw new SharkKBException("sorry, this implementation works with in memo shark kb implementation only");
            }

            InMemoGenericTagStorage tagStorage = imset.getTagStorage();
            sn = new InMemoSemanticNet(tagStorage);
        }

        return sn;
    }
}