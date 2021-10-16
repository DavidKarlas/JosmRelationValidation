package org.openstreetmap.josm.plugins.davidkarlas.RelationValidationPlugin;

import org.openstreetmap.josm.data.validation.OsmValidator;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

public class RelationValidationPlugin extends Plugin {
    public RelationValidationPlugin(PluginInformation info) {
        super(info);
        OsmValidator.addTest(RelationValidationTest.class);
    }
}
