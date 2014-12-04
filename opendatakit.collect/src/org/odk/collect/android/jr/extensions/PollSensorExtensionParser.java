package org.odk.collect.android.jr.extensions;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.IDataReference;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.model.xform.XPathReference;
import org.javarosa.xform.parse.IElementHandler;
import org.javarosa.xform.parse.XFormParser;
import org.kxml2.kdom.Element;

import android.content.Context;

/**
 * Handler for <pollsensor> tags, which get processed by PollSensorActions.
 * @author jschweers
 */
public class PollSensorExtensionParser implements IElementHandler {
    /**
     * (non-Javadoc)
     * @see org.javarosa.xform.parse.IElementHandler#handle(org.javarosa.xform.parse.XFormParser, org.kxml2.kdom.Element, java.lang.Object)
     * 
     * Handle pollsensor node, creating a new PollSensor action with the node that sensor data will be written to.
     * @param p Parser
     * @param e pollsensor Element
     * @param parent FormDef for the form being parsed
     */
    @Override
    public void handle(XFormParser p, Element e, Object parent) {
        String event = e.getAttributeValue(null, "event");
        FormDef form = (FormDef) parent;
        PollSensorAction action;
        
        String ref = e.getAttributeValue(null, "ref");
        if (ref != null) {
            IDataReference dataRef = new XPathReference(ref);
            dataRef = XFormParser.getAbsRef(dataRef, TreeReference.rootRef());
            TreeReference treeRef = FormInstance.unpackReference(dataRef);
            p.registerActionTarget(treeRef);
            action = new PollSensorAction(treeRef);
        }
        else {
            action = new PollSensorAction();
        }

        form.registerEventListener(event, action);
    }
    
}
