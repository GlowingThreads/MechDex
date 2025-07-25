package mdex.faces;

import mdex.model.KeySwitch;
import mdex.service.KeySwitchService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.Setter;
import net.datafaker.Faker;
import org.omnifaces.util.Messages;
import org.primefaces.PrimeFaces;

import java.io.Serializable;
import java.util.List;

/**
 * This Jakarta Faces backing bean class contains the data and event handlers
 * to perform CRUD operations using a PrimeFaces DataTable configured to perform CRUD.
 */
@Named("currentKeySwitchCrudView")
@ViewScoped // create this object for one HTTP request and keep in memory if the next is for the same page
public class KeySwitchCrudView implements Serializable {

    @Inject
    @Named("firebaseHttpClientKeySwitchService")
    private KeySwitchService keySwitchService;


    /**
     * The selected KeySwitch instance to create, edit, update or delete.
     */
    @Getter
    @Setter
    private KeySwitch selectedKeySwitch;

    /**
     * The unique name of the selected KeySwitch instance.
     */
    @Getter
    @Setter
    private String selectedId;

    /**
     * The list of KeySwitch objects fetched from the Firebase Realtime Database
     */
    @Getter
    private List<KeySwitch> keySwitchs;

    /**
     * Fetch all KeySwitch from the Firebase Realtime Database.
     * <p>
     * If FacesContext message sent from init() method annotated with @PostConstruct in the Faces backing bean class are not shown on page:
     * 1) Remove the @PostConstruct annotation from the Faces backing bean class
     * 2) Add metadata tag shown below to the page to execute the init() method
     * <f:metadata>
     * <f:viewParam name="dummy" />
     * <f:event type="postInvokeAction" listener="#{currentBeanView.init}" />
     * </f:metadata>
     */
    @PostConstruct
    public void init() {
        try {
            keySwitchs = keySwitchService.getAllKeySwitchs();
        } catch (Exception e) {
            Messages.addGlobalError("Error getting keySwitchs %s", e.getMessage());
        }
    }

    /**
     * Event handler for the New button on the Faces crud page.
     * Create a new selected KeySwitch instance to enter data for.
     */
    public void onOpenNew() {
        selectedKeySwitch = new KeySwitch();
        selectedId = null;
    }


    /**
     * Event handler to generate fake data using DataFaker.
     *
     * @link <a href="https://www.datafaker.net/documentation/getting-started/">Getting started with DataFaker</a>
     */
    public void onGenerateData() {
        try {
            var faker = new Faker();
            selectedKeySwitch.setSwitchName(faker.mood().tone()+' '+faker.animal().name());
            selectedKeySwitch.setSwitchType("Linear Switch");
            selectedKeySwitch.setCompany(faker.darkSouls().covenants());
            selectedKeySwitch.setActuationForce("55ug");
            selectedKeySwitch.setSwitchTravel("2.2mm");


        } catch (Exception e) {
            Messages.addGlobalError("Error generating data {0}", e.getMessage());
        }

    }

    /**
     * Event handler for Save button to create or update data.
     */
    public void onSave() {
        try {

            // If selectedId is null then create new data otherwise update current data
            if (selectedId == null) {
                KeySwitch createdKeySwitch = keySwitchService.createKeySwitch(selectedKeySwitch);

                // Send a Faces info message that create was successful
                Messages.addGlobalInfo("Create was successful with generated id of {0}", createdKeySwitch.getId());
                // Reset the selected instance to null
                selectedKeySwitch = null;

            } else {
                keySwitchService.updateKeySwitch(selectedKeySwitch);

                Messages.addGlobalInfo("Update was successful");

            }

            // Fetch a list of objects from the Firebase RTDB
            keySwitchs = keySwitchService.getAllKeySwitchs();
            PrimeFaces.current().ajax().update("dialogs:messages", "form:dt-KeySwitchs");

            // Hide the PrimeFaces dialog
            PrimeFaces.current().executeScript("PF('manageKeySwitchDialog').hide()");
        } catch (RuntimeException ex) { // handle application generated exceptions
            Messages.addGlobalError(ex.getMessage());
        } catch (Exception ex) {    // handle system generated exceptions
            Messages.addGlobalError("Save not successful.");
            handleException(ex);
        }

    }

    /**
     * Event handler for Delete to delete selected data.
     */
    public void onDelete() {
        try {
            // Get the unique name of the Json object to delete
            selectedId = selectedKeySwitch.getId();
            keySwitchService.deleteKeySwitchById(selectedId);
            Messages.addGlobalInfo("Delete was successful for id of {0}", selectedId);
            // Fetch new data from Firebase
            keySwitchs = keySwitchService.getAllKeySwitchs();

            PrimeFaces.current().ajax().update("dialogs:messages", "form:dt-KeySwitchs");
        } catch (RuntimeException ex) { // handle application generated exceptions
            Messages.addGlobalError(ex.getMessage());
        } catch (Exception ex) {    // handle system generated exceptions
            Messages.addGlobalError("Delete not successful.");
            handleException(ex);
        }

    }

    /**
     * This method is used to handle exceptions and display root cause to user.
     *
     * @param ex The Exception to handle.
     */
    protected void handleException(Exception ex) {
        StringBuilder details = new StringBuilder();
        Throwable causes = ex;
        while (causes.getCause() != null) {
            details.append(ex.getMessage());
            details.append("    Caused by:");
            details.append(causes.getCause().getMessage());
            causes = causes.getCause();
        }
        Messages.create(ex.getMessage()).detail(details.toString()).error().add("errors");
    }

}