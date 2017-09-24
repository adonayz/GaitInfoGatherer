package edu.wpi.gaitinfogatherer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import org.researchstack.backbone.answerformat.AnswerFormat;
import org.researchstack.backbone.answerformat.BirthDateAnswerFormat;
import org.researchstack.backbone.answerformat.ChoiceAnswerFormat;
import org.researchstack.backbone.answerformat.TextAnswerFormat;
import org.researchstack.backbone.model.Choice;
import org.researchstack.backbone.model.ConsentDocument;
import org.researchstack.backbone.model.ConsentSection;
import org.researchstack.backbone.model.ConsentSignature;
import org.researchstack.backbone.step.ConsentDocumentStep;
import org.researchstack.backbone.step.ConsentSignatureStep;
import org.researchstack.backbone.step.ConsentVisualStep;
import org.researchstack.backbone.step.FormStep;
import org.researchstack.backbone.step.InstructionStep;
import org.researchstack.backbone.step.QuestionStep;
import org.researchstack.backbone.step.Step;
import org.researchstack.backbone.task.OrderedTask;
import org.researchstack.backbone.task.Task;
import org.researchstack.backbone.ui.ViewTaskActivity;
import org.researchstack.backbone.ui.step.layout.ConsentSignatureStepLayout;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HomeScreenActivity extends AppCompatActivity {
    private static final int REQUEST_CONSENT = 0;
    private static final int REQUEST_SURVEY  = 1;

    private GaitStep gaitStep;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*setContentView(R.layout.activity_home_screen);

        Button button = (Button) findViewById(R.id.surveyButton);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //requestPermisions();
                displaySurvey();
            }
        });
*/
        displaySurvey();

    }

    private void displayConsent() {
        // 1
        ConsentDocument document = createConsentDocument();

// 2
        List<Step> steps = createConsentSteps(document);

// 3
        Task consentTask = new OrderedTask("consent_task", steps);

// 4
        Intent intent = ViewTaskActivity.newIntent(this, consentTask);
        startActivityForResult(intent, REQUEST_CONSENT);

    }

    private void displaySurvey() {
        List<Step> steps = new ArrayList<>();

        /*InstructionStep instructionStep = new InstructionStep("survey_instruction_step",
                "Alcohol consumption movement survey",
                "This survey..... details...");
        steps.add(instructionStep);*/

        FormStep formStep = new FormStep("subject_info_form", "Fill in test subject information","");

        TextAnswerFormat textFormat = new TextAnswerFormat(20);

        QuestionStep idStep = new QuestionStep("subject_id", "What is the subject's ID?", textFormat);
        idStep.setPlaceholder("Subject ID");
        idStep.setOptional(false);

        AnswerFormat questionFormat = new ChoiceAnswerFormat(AnswerFormat.ChoiceAnswerStyle
                .SingleChoice,
                new Choice<>("Male", 0),
                new Choice<>("Female", 1));

        QuestionStep genderStep = new QuestionStep("gender_step", "What is your gender?", questionFormat);
        genderStep.setPlaceholder("Gender");
        genderStep.setOptional(false);

        BirthDateAnswerFormat birthDateAnswerFormat = new BirthDateAnswerFormat(new Date(), 18, 70);
        QuestionStep bDayStep = new QuestionStep("bday_step", "What is the subject's birth date?", birthDateAnswerFormat);
        bDayStep.setPlaceholder("Birth date");
        bDayStep.setOptional(false);

        QuestionStep weightStep = new QuestionStep("weight_step", "What is the subject's weight?", textFormat);
        weightStep.setPlaceholder("Weight");
        weightStep.setOptional(false);

        QuestionStep heightStep = new QuestionStep("height_step", "What is the subject's height?", textFormat);
        heightStep.setPlaceholder("Height");
        heightStep.setOptional(false);

        formStep.setFormSteps(idStep, genderStep, bDayStep, weightStep, heightStep);

        steps.add(formStep);


        gaitStep = new GaitStep("gait_step");
        gaitStep.setTitle("Walk in a straight line");
        gaitStep.setText("Walk for 30 seconds. 15 seconds forward and 15 seconds back.");
        gaitStep.setDuration(5);
        steps.add(gaitStep);

        InstructionStep summaryStep = new InstructionStep("survey_summary_step",
                "Right. Off you go!",
                "That was easy!");
        steps.add(summaryStep);

        OrderedTask task = new OrderedTask("survey_task", steps);

        Intent intent = ViewTaskActivity.newIntent(this, task);
        startActivityForResult(intent, REQUEST_SURVEY);

    }


    private ConsentDocument createConsentDocument() {

        ConsentDocument document = new ConsentDocument();
        document.setTitle("Demo Consent");
        document.setSignaturePageTitle(R.string.rsb_consent);

        List<ConsentSection> sections = new ArrayList<>();


        sections.add(createSection(ConsentSection.Type.DataGathering, "Data Gathering Info", ""));
        sections.add(createSection(ConsentSection.Type.DataUse, "Data Use Info", ""));
        sections.add(createSection(ConsentSection.Type.TimeCommitment, "Time Commitment Info", ""));

        document.setSections(sections);

        ConsentSignature signature = new ConsentSignature();
        signature.setRequiresName(true);
        signature.setRequiresSignatureImage(true);

        document.addSignature(signature);

        document.setHtmlReviewContent("<div style=\"padding: 10px;\" class=\"header\">" +
                "<h1 style='text-align: center'>Review Consent!</h1></div>");

        return document;
    }

    private ConsentSection createSection(ConsentSection.Type type, String summary, String content) {

        ConsentSection section = new ConsentSection(type);
        section.setSummary(summary);
        section.setHtmlContent(content);

        return section;
    }

    private List<Step> createConsentSteps(ConsentDocument document) {

        List<Step> steps = new ArrayList<>();

        for (ConsentSection section : document.getSections()) {
            ConsentVisualStep visualStep = new ConsentVisualStep(section.getType().toString());
            visualStep.setSection(section);
            visualStep.setNextButtonString(getString(R.string.rsb_next));
            steps.add(visualStep);
        }

        ConsentDocumentStep documentStep = new ConsentDocumentStep("consent_doc");
        documentStep.setConsentHTML(document.getHtmlReviewContent());
        documentStep.setConfirmMessage(getString(R.string.rsb_consent_review_reason));

        steps.add(documentStep);

        ConsentSignature signature = document.getSignature(0);

        if (signature.requiresName()) {
            TextAnswerFormat format = new TextAnswerFormat();
            format.setIsMultipleLines(false);

            QuestionStep fullName = new QuestionStep("consent_name_step", "Please enter your full name",
                    format);
            fullName.setPlaceholder("Full name");
            fullName.setOptional(false);
            steps.add(fullName);
        }

        if (signature.requiresSignatureImage()) {

            ConsentSignatureStep signatureStep = new ConsentSignatureStep("signature_step");
            signatureStep.setTitle(getString(R.string.rsb_consent_signature_title));
            signatureStep.setText(getString(R.string.rsb_consent_signature_instruction));
            signatureStep.setOptional(false);

            signatureStep.setStepLayoutClass(ConsentSignatureStepLayout.class);

            steps.add(signatureStep);
        }

        return steps;
    }

}
