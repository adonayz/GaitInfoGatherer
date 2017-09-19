package edu.wpi.gaitinfogatherer;

import org.researchstack.backbone.step.Step;

/**
 * Created by Adonay on 9/11/2017.
 */

public class GaitStep extends Step {
    private int mDuration;

    public GaitStep(String identifier)
    {
        super(identifier);
        setOptional(false);
        setStepLayoutClass(GaitStepLayout.class);
    }

    public int getDuration() {
        return mDuration;
    }

    public void setDuration(int duration) {
        mDuration = duration;
    }
}