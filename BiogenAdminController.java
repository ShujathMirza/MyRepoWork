package com.biogen.zinbryta.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.biogen.zinbryta.common.CommonUtils;
import com.biogen.zinbryta.dto.UserDto;
import com.biogen.zinbryta.dto.biogen.admin.ChangePatientPractice;
import com.biogen.zinbryta.dto.biogen.admin.EnrolledButNoPortalAccountUserList;
import com.biogen.zinbryta.dto.biogen.admin.FindUserForm;
import com.biogen.zinbryta.dto.biogen.admin.PatientPracticeInfo;
import com.biogen.zinbryta.event.OnMergePracticeEmailEvent;
import com.biogen.zinbryta.event.OnUserEmailChangeEvent;
import com.biogen.zinbryta.persistence.model.Hospital;
import com.biogen.zinbryta.persistence.model.HospitalLocation;
import com.biogen.zinbryta.persistence.model.HospitalLocationMap;
import com.biogen.zinbryta.persistence.model.UserMap;
import com.biogen.zinbryta.persistence.model.UserProfile;
import com.biogen.zinbryta.service.BiogenAdminService;
import com.biogen.zinbryta.service.PortalUserService;
import com.biogen.zinbryta.service.UserProfileService;
import com.biogen.zinbryta.util.ConvertObjectToJson;

@Controller
@SessionAttributes({"loggedInUser"})
public class BiogenAdminController {

  private static final org.slf4j.Logger LOGGER =
      LoggerFactory.getLogger(BiogenAdminController.class);

  @Autowired
  BiogenAdminService biogenAdminService;
  @Autowired
  private UserProfileService userProfileService;
  @Autowired
  private ApplicationEventPublisher applicationEventPublisher;
  @Autowired
  private PortalUserService portalUserService;

  /**
   * Get to the Biogen Admin Login Page
   * 
   * @param model
   * @return
   */
  @RequestMapping(value = {"/biogenadmin", "/biogenadmin/login"}, method = RequestMethod.GET)
  public String biogenAdminLogin(ModelMap model) {
    return "biogenAdminLogin";
  }

  /**
   * Get to the Biogen Admin Main Page
   * 
   * @param model
   * @return
   */
  @RequestMapping(value = {"/biogenadmin/mainpage"}, method = RequestMethod.GET)
  public String biogenAdminMainPage(ModelMap model) {
    return "biogenAdminMainPage";
  }

  /**
   * Get to the Biogen Admin Main Page with Find practice Fields
   * 
   */
  @RequestMapping(value = {"/biogenadmin/mainpageFindPractice"}, method = RequestMethod.GET)
  public String biogenAdminMainPageFindPractice(ModelMap model) {
    return "biogenAdminFindPracticeMainPage";
  }

  /**
   * Get to the createportalaccount page from the BiogenAdmin Main Page
   * 
   * @param model
   * @return
   */
  @RequestMapping(value = {"/biogenadmin/createportalaccount"}, method = RequestMethod.GET)
  public String biogenAdminMainPageCreatePortalAccount(ModelMap model) {

    List<EnrolledButNoPortalAccountUserList> lstEnrolledPrescribers_WithNoPortalAccount =
        biogenAdminService.getAllEnrolledPrescribersWhoDoNotHaveAPortalAccount();

    String strEnrolledPrescribers_WithNoPortalAccount = "";
    if (lstEnrolledPrescribers_WithNoPortalAccount != null
        && lstEnrolledPrescribers_WithNoPortalAccount.size() > 0)
      strEnrolledPrescribers_WithNoPortalAccount =
          new ConvertObjectToJson(lstEnrolledPrescribers_WithNoPortalAccount).jsonToString();

    model.addAttribute("lstEnrolledPrescribers_WithNoPortalAccount",
        strEnrolledPrescribers_WithNoPortalAccount);

    return "biogenAdminCreatePortalAccount";
  }
  /**
   * Get to the Portal Edit Practice
   * 
   * @param model
   * @return
   */
  @RequestMapping(value = "/biogenadmin/adminEditPractice-{hospitalId}", method = RequestMethod.GET)
  public String editPractice(ModelMap model, @PathVariable("hospitalId") long hospitalId) {

    try {
      Hospital hospital = new Hospital();
      hospital = portalUserService.getHospital(hospitalId);
      model.addAttribute("hospital", hospital);
    } catch (Exception er) {
      er.printStackTrace();
    }
    return "editPractice";
  }

  /**
   * Get to the Biogen Admin Find User
   * 
   * @param model
   * @return
   */
  @RequestMapping(value = {"/biogenadmin/finduser"}, method = RequestMethod.GET)
  public String biogenAdminFindUser(ModelMap model) {
    return "biogenAdminFindUser";
  }

  /**
   * Get to the Biogen Admin Find User
   * 
   * @param model
   * @return
   */
  @RequestMapping(value = {"/biogenadmin/finduser"}, method = RequestMethod.POST)
  public String biogenAdminFindUser_Post(ModelMap model,
      @ModelAttribute("findUserForm") FindUserForm findUserFormFromUI) {

    String strUserList = biogenAdminService.getUsers(findUserFormFromUI);
    model.addAttribute("strUserList", strUserList);

    return "biogenAdminFindUser";
  }

  /**
   * Get to the Biogen Admin Find Practice
   * 
   * @param model
   * @return
   */
  @RequestMapping(value = {"/biogenadmin/findpractice"}, method = RequestMethod.GET)
  public String biogenAdminFindPractice(ModelMap model) {
    return "biogenAdminFindPractice";
  }

  /**
   * Get to the Biogen Admin Find Practice
   * 
   * @param model
   * @return
   */
  @RequestMapping(value = {"/biogenadmin/findpractice"}, method = RequestMethod.POST)
  public String biogenAdminFindPractice_Post(ModelMap model,
      @ModelAttribute("findPracticeForm") Hospital findPracticeFormFormUI) {

    String strHospitalList = biogenAdminService.getHospitals(findPracticeFormFormUI);
    model.addAttribute("strHospitalList", strHospitalList);
    return "biogenAdminFindPractice";
  }

  /**
   * Get to the Biogen Admin Manage User Information
   * 
   * @param model
   * @return
   */
  @RequestMapping(value = {"/biogenadmin/manageuser-{userName}-{userProfileId}"},
      method = RequestMethod.GET)
  public String biogenAdminManageUser(ModelMap model, @PathVariable("userName") String userName,
      @PathVariable("userProfileId") long userProfileId) {
    model.addAttribute("userName", userName);
    model.addAttribute("userProfileId", userProfileId);
    LOGGER.debug(userProfileId + "User profile id pathe variable ");
    UserProfile user = userProfileService.findByUserName(userName);
    model.addAttribute("user", user);

    String strPracticeListPrescriber = biogenAdminService.findPracticeUserProfile(userProfileId);
    model.addAttribute("strPracticeListPrescriber", strPracticeListPrescriber);
    return "biogenAdminManageUser";
  }

  /**
   * Get to the Biogen Admin Manage Practice
   * 
   * @param model
   * @return
   */
  @RequestMapping(value = {"/biogenadmin/managepractice-{hospitalId}-{hospitalAffiliation}"},
      method = RequestMethod.GET)
  public String biogenAdminManagePractice(ModelMap model,
      @PathVariable("hospitalId") long hospitalId,
      @PathVariable("hospitalAffiliation") String hospitalAffiliation) {
    try {

      List<HospitalLocationMap> lstHospitalsAndLocations =
          portalUserService.getHospitalLocationMap(hospitalId);
      model.addAttribute("lstHospitalsAndLocations", lstHospitalsAndLocations);
      model.addAttribute("hospitalAffiliation", hospitalAffiliation); // Hospital / Practice Name
      // --------------------------------------------------------------------------

      List<UserMap> lstUsers = portalUserService.getUsers(hospitalId);
      // Get the Users of the Prescriber who created the Portal User or Portal
      // Admin or Prescriber, via the Portal Admin module
      model.addAttribute("lstUsers", lstUsers);

    } catch (Exception ere) {
      LOGGER.debug(
          "/biogenadmin/managepractice-{hospitalId}-{hospitalAffiliation} " + ere.getMessage());
    }
    return "biogenAdminManagePractice";
  }

  /**
   * Get to the Biogen Admin Create New User
   * 
   * @param model
   * @return
   */
  @RequestMapping(value = {"/biogenadmin/createnewuser"}, method = RequestMethod.GET)
  public String biogenAdminCreateNewUser(ModelMap model) {
    return "biogenAdminCreateNewUser";
  }

  /**
   * Get to the Patient Start Form Opt In
   * 
   * @param model
   * @return
   */
  @RequestMapping(value = {"/patientstartform", "/patientstartform/optin"},
      method = RequestMethod.GET)
  public String patientStartFormOptIn(ModelMap model) {
    return "dpatientStartFormOptIn";
  }

  /**
   * Get to the Patient Start Form Additional Info
   * 
   * @param model
   * @return
   */
  @RequestMapping(value = {"/patientstartform/addinfo"}, method = RequestMethod.GET)
  public String patientStartFormAddInfo(ModelMap model) {
    return "dpatientStartFormAdditionalInfo";
  }

  /**
   * Get to the Patient Start Form Prescription
   * 
   * @param model
   * @return
   */
  @RequestMapping(value = {"/patientstartform/prescription"}, method = RequestMethod.GET)
  public String patientStartFormPrescription(ModelMap model) {
    return "dpatientStartFormPrescription";
  }

  /**
   * Get to the Patient Start Form Completed
   * 
   * @param model
   * @return
   */
  @RequestMapping(value = {"/patientstartform/completed"}, method = RequestMethod.GET)
  public String patientStartFormCompleted(ModelMap model) {
    return "dpatientStartFormComplete";
  }

  /**
   * Get to the Patient Start Form Print
   * 
   * @param model
   * @return
   */
  @RequestMapping(value = {"/patientstartform/printforms"}, method = RequestMethod.GET)
  public String patientStartFormPrintForms(ModelMap model) {
    return "dpatientStartFormPrintForms";
  }

  /**
   * Get to the Biogen Admin Change Email
   * 
   * @param model
   * @return
   */
  @RequestMapping(value = {"/biogenadmin/changeemail-{userName}"}, method = RequestMethod.GET)
  public String biogenAdminChangeEmail(ModelMap model, @PathVariable("userName") String userName) {

    UserDto userDto = new UserDto();
    model.addAttribute("userName", userName);
    model.addAttribute("userDto", userDto);
    UserProfile user = userProfileService.findByUserName(userName);
    model.addAttribute("user", user);
    return "biogenAdminChangeEmail";
  }

  /**
   * POST - Changing the Email of the User
   * 
   * @param model
   * @param loggedInUser
   * @param userDto
   * @param redirectAttributes
   * @param result
   * @return
   */

  @RequestMapping(value = {"/biogenadmin/changeemailDb"}, method = RequestMethod.POST)
  public String biogenAdminChangeEmailDB(ModelMap model, UserProfile loggedInUser,
      @ModelAttribute("userDto") UserDto userDto, final RedirectAttributes redirectAttributes,
      BindingResult result, HttpServletRequest request) {

    userDto.setEmail(userDto.getEmail());
    UserProfile oldUser = userProfileService.findByUserName(userDto.getEmail());// Gets UserProfile
                                                                                // Object for older
                                                                                // email address
    // Sends Email to Older
    applicationEventPublisher
        .publishEvent(new OnUserEmailChangeEvent(oldUser, CommonUtils.getAppUrl(request)));
    // Email
    userDto.setNewEmail(userDto.getNewEmail()); // Setting the New User Email
    userProfileService.updateUserEmail(userDto); // Updates the User email in the User Profile table
    UserProfile updatedUserProfile = userProfileService.findByUserName(userDto.getNewEmail());// User

    // Sends email to new
    applicationEventPublisher.publishEvent(
        new OnUserEmailChangeEvent(updatedUserProfile, CommonUtils.getAppUrl(request)));

    model.addAttribute("updatedUser", updatedUserProfile);
    return "biogenAdminChangeEmailComplete";
  }

  /**
   * Get to the Biogen Admin Change Email Complete
   * 
   * @param model
   * @return
   */
  @RequestMapping(value = {"/biogenadmin/changeemailcomplete"}, method = RequestMethod.GET)
  public String biogenAdminChangeEmailComplete(ModelMap model) {
    return "biogenAdminChangeEmailComplete";
  }

  /**
   * Get to the Biogen Admin Change Patient Practice
   * 
   * @param model
   * @return
   */
  @RequestMapping(value = {"/biogenadmin/changepatientpractice"}, method = RequestMethod.GET)
  public String biogenAdminChangePatientPractice(ModelMap model) {
    List<Hospital> lstHospital = portalUserService.getAllHospital();
    model.addAttribute("lstHospital", lstHospital);

    model.addAttribute("strPatientListForSelectedPractice", "[]");

    return "biogenAdminChangePatientPractice";
  }

  /**
   * 
   * @param model
   * @return
   */
  @RequestMapping(value = {"/biogenadmin/changepatientpractice"}, method = RequestMethod.POST)
  public String biogenAdminChangePatientPractice_POST(ModelMap model,
      @ModelAttribute("changePatientPractice") ChangePatientPractice changePatientPractice_FromUI) {
    List<Hospital> lstHospital = portalUserService.getAllHospital();
    model.addAttribute("lstHospital", lstHospital);

    List<PatientPracticeInfo> lstPatientPracticeInfo =
        biogenAdminService.getPatients(changePatientPractice_FromUI.getHospitalIdFrom());


    String strPatientListForSelectedPractice = "[]";
    if (lstPatientPracticeInfo != null && lstPatientPracticeInfo.size() > 0)
      strPatientListForSelectedPractice =
          new ConvertObjectToJson(lstPatientPracticeInfo).jsonToString();

    model.addAttribute("strPatientListForSelectedPractice", strPatientListForSelectedPractice);

    return "biogenAdminChangePatientPractice";
  }

  /**
   * Called when user clicks, the button "Update Selected" frm the - <br/>
   * Change Patient PRactice Affiliation page
   * 
   * @param model
   * @return
   */
  @RequestMapping(value = {"/biogenadmin/amendPatientPractice"}, method = RequestMethod.POST)
  public String biogenAdminAmendPatientPractice(ModelMap model,
      @ModelAttribute("changePatientPractice") ChangePatientPractice changePatientPractice_FromUI,
      @ModelAttribute("loggedInUser") UserProfile loggedInUser) {
    List<Hospital> lstHospital = portalUserService.getAllHospital();
    model.addAttribute("lstHospital", lstHospital);

    biogenAdminService.saveChangePatientPractice(changePatientPractice_FromUI, loggedInUser);

    model.addAttribute("strPatientListForSelectedPractice", "[]");

    return "redirect:/biogenadmin/mainpage";
  }


  /**
   * Get to the Biogen Admin find Practice Main Page
   * 
   * @param model
   * @return
   */
  @RequestMapping(value = {"/biogenadmin/findpracticemp"}, method = RequestMethod.GET)
  public String biogenAdminFindPracticeMP(ModelMap model) {
    return "biogenAdminFindPracticeMainPage";
  }

  /**
   * Get to the Biogen Admin find Practice Main Page
   * 
   * @param model
   * @return
   */
  @RequestMapping(value = {"/biogenadmin/mergepractice"}, method = RequestMethod.GET)
  public String biogenAdminMergePractice(ModelMap model) {
    return "biogenAdminMergePractice";
  }

  /**
   * Get to the Biogen Admin find Practice Main Page
   * 
   * @param model
   * @return
   */
  @RequestMapping(value = {"/biogenadmin/mergepractice"}, method = RequestMethod.POST)
  public String biogenAdminMergePractice_Practice1_Post(ModelMap model,
      @ModelAttribute("findPractice1Form") Hospital findPractice1FormFormUI) {
    String strHospitalList1 = biogenAdminService.getHospitals(findPractice1FormFormUI);
    model.addAttribute("strHospitalList1", strHospitalList1);
    String strHospitalList2 = biogenAdminService.getHospitals(findPractice1FormFormUI);
    model.addAttribute("strHospitalList1", strHospitalList1);
    return "biogenAdminMergePractice";
  }



  /**
   * Get to the Biogen Admin Merge Practice Results
   * 
   * @param model
   * @return
   */
  @RequestMapping(value = {"/biogenadmin/mergepracticeresults"}, method = RequestMethod.GET)
  public String biogenAdminMergePracticeResults(ModelMap model) {
    return "biogenAdminMergePracticeResults";
  }

  /**
   * TODO POST call to get the hospital results from the DB
   * 
   * Get to the Biogen Admin Merge Practice Results Page1
   * 
   * @param model
   * @return
   */
  @RequestMapping(value = {"/biogenadmin/mergePracticeResults"}, method = RequestMethod.POST)
  public String biogenAdminMergePracticeresults_Post(ModelMap model,
      @ModelAttribute("findPracticeMergeResultsForm") Hospital findPracticeFormFormUI) {

    String strHospitalList = biogenAdminService.getHospitals(findPracticeFormFormUI);
    model.addAttribute("strHospitalList", strHospitalList);
    return "biogenAdminMergePracticeResults";
  }

  /**
   * TODO GET call to get the hospital results from the DB
   * 
   * Get to the Biogen Admin Merge Practice Results Page2
   * 
   * @param model
   * @return
   */
  @RequestMapping(value = {"/biogenadmin/mergePracticeResultsPage2-{hospId}"}, method = RequestMethod.GET)
  public String biogenAdminMergePracticeresultsPage2_Get(ModelMap model,
      @ModelAttribute("findPracticeMergeResultsform2") Hospital findPracticeFormFormUI , @PathVariable("hospId")Long hospId ) {
    model.addAttribute("hospId",hospId);
    System.out.println(hospId+"Hospital ID");
    String strHospitalList = biogenAdminService.getHospitals(findPracticeFormFormUI);
    model.addAttribute("strHospitalList", strHospitalList);
    return "biogenAdminMergePracticeResultsPage2";
  }

  /**
   * TODO POST call to get the hospital results from the DB
   * 
   * Get to the Biogen Admin Merge Practice Results Page2
   * 
   * @param model
   * @return
   */
  @RequestMapping(value = {"/biogenadmin/mergePracticeResultsPage2"}, method = RequestMethod.POST)
  public String biogenAdminMergePracticeresultsPage2_Post(ModelMap model,
      @ModelAttribute("findPracticeMergeResultsform2") Hospital findPracticeFormFormUI) {

    String strHospitalList = biogenAdminService.getHospitals(findPracticeFormFormUI);
    model.addAttribute("strHospitalList", strHospitalList);
    return "biogenAdminMergePracticeResultsPage2";
  }

  /**
   * Get to the Biogen Admin Merge Practice Review Selection
   * 
   * @param model
   * @return
   */
  @RequestMapping(value = {"/biogenadmin/mergepracticereviewselection-{hospId1}-{hospId2}"}, method = RequestMethod.GET)
  public String biogenAdminMergePracticeReviewSelection(ModelMap model,@PathVariable("hospId1")Long hospId1, @PathVariable("hospId2")Long hospId2) {
    
    Hospital hospital1 = new Hospital();
    hospital1 = biogenAdminService.getHospital(hospId1); // Get Hospital Object for Hospital 1  
    model.addAttribute("hospital1",hospital1);
    Hospital hospital2 = new Hospital();   
    hospital2=biogenAdminService.getHospital(hospId2); // Get Hospital Object for Hospital2    
    model.addAttribute("hospital2",hospital2);
    return "biogenAdminMergePracticeReviewSelection";
  }

  /**
   * Get to the Biogen Admin Merge Practice Review Selected Page when First Practice is Selected (If winning Practice is Practice-1 ) 
   * 
   * @param model
   * @return
   */
  @RequestMapping(value = {"/biogenadmin/mergepracticereviewselectedpage1-{hospitalId1}-{hospitalId2}"},
      method = RequestMethod.GET)
  public String biogenAdminMergePracticeReviewSelectedPage(ModelMap model , @PathVariable("hospitalId1")Long hospId1, @PathVariable("hospitalId2")Long hospId2 ) {
   
    Hospital hospital1 = new Hospital();
    hospital1 = biogenAdminService.getHospital(hospId1);// Get Hospital Object for Hospital 1
    model.addAttribute("hospital1",hospital1);
    Hospital hospital2 = new Hospital();   
    hospital2=biogenAdminService.getHospital(hospId2); // Get Hospital Object for Hospital2    
    model.addAttribute("hospital2",hospital2);
    return "biogenAdminMergePracticeReviewSelectedPage1";
  }
  
  /**
   * Get to the Biogen Admin Merge Practice Review Selected Page when Second Practice is Selected (If winning Practice is Practice-2 ) 
   * 
   * @param model
   * @return
   */
  @RequestMapping(value = {"/biogenadmin/mergepracticereviewselectedpage2-{hospitalId1}-{hospitalId2}"},
      method = RequestMethod.GET)
  public String biogenAdminMergePracticeReviewSelectedPage2(ModelMap model , @PathVariable("hospitalId1")Long hospId1, @PathVariable("hospitalId2")Long hospId2 ) {
   
    Hospital hospital1 = new Hospital();
    hospital1 = biogenAdminService.getHospital(hospId1);// Get Hospital Object for Hospital 1
    model.addAttribute("hospital1",hospital1);
    Hospital hospital2 = new Hospital();   
    hospital2=biogenAdminService.getHospital(hospId2); // Get Hospital Object for Hospital2    
    model.addAttribute("hospital2",hospital2);
    return "biogenAdminMergePracticeReviewSelectedPage";
  }
  
  /**
   * Get to the Biogen Admin Merge Practice Confirmation - When Practice 1 is confirmed 
   * 
   * @param model
   * @return
   */
  @RequestMapping(value = {"/biogenadmin/mergepracticeconfirm1-{hospitalId1}-{hospitalId2}"}, method = RequestMethod.GET)
  public String biogenAdminMergePracticeConfirm1(ModelMap model ,@PathVariable("hospitalId1")Long hospId1, @PathVariable("hospitalId2")Long hospId2) {
    
    Hospital hospital1 = new Hospital();
    hospital1 = biogenAdminService.getHospital(hospId1);// Get Hospital Object for Hospital 1
    model.addAttribute("hospital1",hospital1);
    Hospital hospital2 = new Hospital();   
    hospital2=biogenAdminService.getHospital(hospId2); // Get Hospital Object for Hospital2    
    model.addAttribute("hospital2",hospital2);
    
    Hospital winningHospital=hospital1;
    Hospital loosingHospital=hospital2;
    
    UserProfile userProfile = hospital1.getUserProfile();
    //added the two hospital objects to pass in the email 
    applicationEventPublisher
    .publishEvent(new OnMergePracticeEmailEvent(winningHospital,loosingHospital,userProfile));
    return "biogenAdminMergePracticeConfirmation1";
  }
  
  /**
   * Get to the Biogen Admin Merge Practice Confirmation - When Practice 2 is confirmed 
   * 
   * @param model
   * @return
   */
  
  @RequestMapping(value = {"/biogenadmin/mergepracticeconfirm2-{hospitalId1}-{hospitalId2}"}, method = RequestMethod.GET)
  public String biogenAdminMergePracticeConfirm2(ModelMap model ,@PathVariable("hospitalId1")Long hospId1, @PathVariable("hospitalId2")Long hospId2) {
    
    Hospital hospital1 = new Hospital();
    hospital1 = biogenAdminService.getHospital(hospId1);// Get Hospital Object for Hospital 1
    model.addAttribute("hospital1",hospital1);
    Hospital hospital2 = new Hospital();   
    hospital2=biogenAdminService.getHospital(hospId2); // Get Hospital Object for Hospital2    
    model.addAttribute("hospital2",hospital2);
    Hospital winningHospital=hospital2;
    Hospital loosingHospital=hospital1;
    
    UserProfile userProfile = hospital1.getUserProfile();
  // added the two hospital objects to pass in the email 
    applicationEventPublisher
    .publishEvent(new OnMergePracticeEmailEvent(winningHospital,loosingHospital,userProfile));
    return "biogenAdminMergePracticeConfirmation";
  }

  /**
   * Get to the biogenAdminAddNewUser Page
   * 
   * @param model
   * @return
   */
  @RequestMapping(value = {"/biogenadmin/addNewUser"}, method = RequestMethod.GET)
  public String biogenAdminAddNewUser(ModelMap model) {
    return "biogenAdminAddNewUser";
  }

  /**
   * Get to the biogenAdminAddNewLocation Page
   * 
   * @param model
   * @return
   */
  @RequestMapping(value = {"/biogenadmin/addNewLocation"}, method = RequestMethod.GET)
  public String biogenAdminAddNewLocation(ModelMap model) {

    return "biogenAdminAddNewLocation";
  }

  @RequestMapping(value = "/biogenadmin/addNewLocation", method = RequestMethod.POST)
  public String biogenAddNewLocationPOST(@ModelAttribute("loggedInUser") UserProfile loggedInUser,
      @ModelAttribute("biogenAdminAddNewLocationForm") HospitalLocation hospitalLocationInfoFromUI,
      ModelMap model, final RedirectAttributes redirectAttributes) {

    try {
      // Add a record in UserProfile table & (prescriber OR PortalUSer table)
      biogenAdminService.addNewLocation(hospitalLocationInfoFromUI);
    } catch (Exception ere) {
      LOGGER.debug(ere.getMessage());
    }
    return "biogenAdminManageUser";
  }

  /**
   * Get to the biogenAdminAddNewPractice Page
   * 
   * @param model
   * @return
   */
  @RequestMapping(value = {"/biogenadmin/addNewPractice"}, method = RequestMethod.GET)
  public String biogenAdminAddNewPractice(ModelMap model) {
    return "biogenAdminAddNewPractice";
  }

  /**
   * Get to the biogenAdminAddNewPractice Page
   * 
   * @param model
   * @return
   */
  @RequestMapping(value = {"/biogenadmin/mergePracticeUpdate"}, method = RequestMethod.GET)
  public String biogenAdminMergePracticeUpdate(ModelMap model) {
    return "biogenAdminAddNewPractice";
  }
}

