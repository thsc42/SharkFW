package net.sharkfw.knowledgeBase.persistent.fileDump;

import net.sharkfw.knowledgeBase.SemanticTag;
import net.sharkfw.knowledgeBase.SpatialSemanticTag;
import net.sharkfw.knowledgeBase.geom.SharkGeometry;

/**
 * Created by j4rvis on 2/27/17.
 */
public class FileDumpSpatialSemanticTag extends FileDumpSemanticTag implements SpatialSemanticTag {

    private final SpatialSemanticTag spatialSemanticTag;

    public FileDumpSpatialSemanticTag(FileDumpSharkKB fileDumpSharkKB, SpatialSemanticTag tag) {
        super(fileDumpSharkKB, tag);
        spatialSemanticTag = tag;
    }

    @Override
    public SharkGeometry getGeometry() {
        return this.spatialSemanticTag.getGeometry();
    }
}