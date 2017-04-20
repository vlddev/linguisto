/*
#######################################################################
#
#  Linguisto Portal
#
#  Copyright (c) 2017 Volodymyr Vlad
#
#######################################################################
*/

package org.linguisto.webui.listener;

import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;

public class DebugPhaseListener implements PhaseListener {

	private static final long serialVersionUID = -8432770814609679609L;

	private static final Logger log = Logger.getLogger(DebugPhaseListener.class.getName());

    public void beforePhase(PhaseEvent phaseEvent) {
        if (log.isLoggable(Level.FINE)) {
            if (phaseEvent.getPhaseId().equals(PhaseId.RESTORE_VIEW)) {
                log.fine("REQUEST START");
            }
            log.fine(new StringBuffer().append("----- BEFORE PHASE ").append(phaseEvent.getPhaseId()).toString());
        }
    }

    public void afterPhase(PhaseEvent phaseEvent) {
        if (log.isLoggable(Level.FINE)) {
            log.fine(new StringBuffer().append("----- AFTER PHASE ").append(phaseEvent.getPhaseId()).toString());
            if (phaseEvent.getPhaseId().equals(PhaseId.RESTORE_VIEW)) {
                try {
                    log.fine("Calling '" + FacesContext.getCurrentInstance().getViewRoot().getViewId() + "'");
                } catch (Exception e) {
                    log.log(Level.SEVERE, "Error", e);
                }
            }
            if (phaseEvent.getPhaseId().equals(PhaseId.RENDER_RESPONSE)) {
                log.fine("REQUEST END");
                try {
                    UIViewRoot viewRoot = FacesContext.getCurrentInstance().getViewRoot();
                    log.fine("Rendered '" + viewRoot.getViewId() + "'");
                    Iterator<String> clientIds = FacesContext.getCurrentInstance().getClientIdsWithMessages();
                    if (clientIds != null && clientIds.hasNext()) {
                        System.out.println("Following errors:");
                        while (clientIds.hasNext()) {
                            String s = clientIds.next();
                            System.out.println(s);
                        }
                    }
                } catch (Exception e) {
                    log.log(Level.SEVERE, "Error", e);
                }
            }
        }
    }

    public PhaseId getPhaseId() {
        return PhaseId.ANY_PHASE;
    }

}