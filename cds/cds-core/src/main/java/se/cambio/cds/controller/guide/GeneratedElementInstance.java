package se.cambio.cds.controller.guide;

import org.openehr.rm.datatypes.basic.DataValue;
import org.openehr.rm.datatypes.text.DvCodedText;

import se.cambio.cds.model.facade.execution.vo.ArchetypeReference;
import se.cambio.cds.model.facade.execution.vo.ContainerInstance;
import se.cambio.cds.model.facade.execution.vo.ElementInstance;

public class GeneratedElementInstance extends ElementInstance{

    private static final long serialVersionUID = 1L;
    private String guideId = null;
    private String gtCode = null;
    
    public GeneratedElementInstance(
	    String id, 
	    DataValue dataValue,
	    ArchetypeReference archetypeReference,
	    ContainerInstance containerInstance, 
	    DvCodedText nullFlavour,
	    String guideId,
	    String gtCode) {
	super(id, dataValue, archetypeReference, containerInstance, nullFlavour);
	this.guideId = guideId;
	this.gtCode = gtCode;
    }
    
    public String getGtCode() {
        return gtCode;
    }
    
    public void setGtCode(String gtCode) {
        this.gtCode = gtCode;
    }
    
    public String getGuideId() {
        return guideId;
    }
    
    public String toString(){
	return "(G)"+super.toString();
    }
}
/*
 *  ***** BEGIN LICENSE BLOCK *****
 *  Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 *  The contents of this file are subject to the Mozilla Public License Version
 *  1.1 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.mozilla.org/MPL/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *
 *  The Initial Developers of the Original Code are Iago Corbal and Rong Chen.
 *  Portions created by the Initial Developer are Copyright (C) 2012-2013
 *  the Initial Developer. All Rights Reserved.
 *
 *  Contributor(s):
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 *  ***** END LICENSE BLOCK *****
 */