/*
 * Copyright 2021 SORCERsoft.org.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package sorcer.core.plexus;

import sorcer.core.context.DesignIntent;
import sorcer.core.service.Transdesign;
import sorcer.core.signature.LocalSignature;
import sorcer.service.*;

import java.rmi.RemoteException;

public class DesignFidelityManager extends FidelityManager {

    // fidelities for design intents
    protected Fidelity intentFidelities;

    // fidelities for design developers
    protected Fidelity developerFidelities;

    public DesignFidelityManager(Design design) throws RemoteException {
        super(design.getName());
        this.mogram = (( Transdesign ) design).getDesignIntent();
        setDesignFidelities(( Transdesign ) design);
    }

    @Override
    public void reconfigure(Fi... fidelities) throws ConfigurationException {
        if (fidelities == null || fidelities.length == 0) {
            return;
        }
        for (Fi fi : fidelities) {
            if (fi.getFiType().equals(Fi.Type.INTENT)) {
                Fi intentFi = (Fi)getSelectFi(fi);
                ((Transdesign)(( DesignIntent )mogram).getSubjectValue()).setDisciplineIntent(( Context ) intentFi.getSelect());
                continue;
            } else if (fi.getFiType().equals(Fi.Type.DEV)) {
                developerFidelities.selectSelect(fi.getName());
            }
            super.reconfigure(fi);
        }
    }

    // manager top select fidelty
    public Object getSelectFi(Fi fi) throws ConfigurationException {
        Object selectFi = null;
        if (fi.getPath() != null && fi.getPath().length() == 0) {
            selectFi = getSelectFi(intentFidelities.getSelects(), (String)fi.getName());
            if (selectFi instanceof Fidelity
                && (((Fidelity)((Fidelity)selectFi).getSelect()).getSelect() instanceof LocalSignature)) {
                try {
                    Signature signatue = ((LocalSignature)((Fidelity)((Fidelity)selectFi).getSelect()).getSelect());
                    ((Fidelity)selectFi).setSelect(((LocalSignature)signatue).initInstance());
                } catch (SignatureException e) {
                    throw new ConfigurationException(e);
                }
            }
            return selectFi;
        } else if (fi.getSelect() != null) {
            selectFi = getSelectFi(intentFidelities.getSelects(), ( String ) fi.getSelect());
        }
        if (selectFi != null && selectFi instanceof Fidelity) {
            selectFi = (( Fidelity ) selectFi).selectSelect(( String ) fi.getPath());
            if (selectFi != null && (((Fidelity)selectFi).getSelect() instanceof MultiFiSlot)
                && ((MultiFiSlot)((Fidelity)selectFi).getSelect()).getMultiFi() != null) {
                selectFi = ((MultiFiSlot)((Fidelity)selectFi).getSelect()).getMultiFi().selectSelect(fi.getName());
            }
        }
        return selectFi;
    }

    public Fidelity getIntentFidelities() {
        return intentFidelities;
    }

    public void setIntentFidelities(ServiceFidelity intentFidelities) {
        this.intentFidelities = intentFidelities;
    }

    public Fidelity getDeveloperFidelities() {
        return developerFidelities;
    }

    public void setDeveloperFidelities(ServiceFidelity developerFidelities) {
        this.developerFidelities = developerFidelities;
    }

    private void setDesignFidelities(Transdesign design) throws RemoteException {
        Fi devFi = design.getDevelopmentFi();
        Fi dznFi = design.getDesignIntent().getMultiFi();
        if (devFi != null) {
            if (devFi instanceof MorphFidelity) {
                morphFidelities.put(devFi.getName(), (MorphFidelity)devFi);
                registerMorphFidelity(( MorphFidelity ) devFi);
                developerFidelities = (( MorphFidelity ) devFi).getFidelity();
            } else {
                developerFidelities = (ServiceFidelity)devFi;
            }
        }
        if (dznFi instanceof MorphFidelity) {
            morphFidelities.put(devFi.getName(), (MorphFidelity)dznFi);
            registerMorphFidelity(( MorphFidelity ) devFi);
        } else {
            intentFidelities =  (ServiceFidelity)dznFi;
        }
        projections = design.getProjections();
    }
}
