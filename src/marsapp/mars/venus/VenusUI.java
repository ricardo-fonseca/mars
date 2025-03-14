package mars.venus;
import mars.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.awt.image.*;


/*
Copyright (c) 2003-2013,  Pete Sanderson and Kenneth Vollmar

Developed by Pete Sanderson (psanderson@otterbein.edu)
and Kenneth Vollmar (kenvollmar@missouristate.edu)

Permission is hereby granted, free of charge, to any person obtaining 
a copy of this software and associated documentation files (the 
"Software"), to deal in the Software without restriction, including 
without limitation the rights to use, copy, modify, merge, publish, 
distribute, sublicense, and/or sell copies of the Software, and to 
permit persons to whom the Software is furnished to do so, subject 
to the following conditions:

The above copyright notice and this permission notice shall be 
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, 
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR 
ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION 
WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

(MIT license, http://www.opensource.org/licenses/mit-license.html)
 */

	/**
	  *  Top level container for Venus GUI.
	  *   @author Sanderson and Team JSpim
	  **/
	  
	  /* Heavily modified by Pete Sanderson, July 2004, to incorporate JSPIMMenu and JSPIMToolbar
	   * not as subclasses of JMenuBar and JToolBar, but as instances of them.  They are both
		* here primarily so both can share the Action objects.
		*/
	
    public class VenusUI extends JFrame{
      VenusUI mainUI;
      public JMenuBar menu;
      JToolBar toolbar;
      MainPane mainPane; 
      RegistersPane registersPane; 
      RegistersWindow registersTab;
      Coprocessor1Window coprocessor1Tab;
      Coprocessor0Window coprocessor0Tab;
      MessagesPane messagesPane;
      JSplitPane splitter, horizonSplitter;
      JPanel north;
   
      private int frameState; // see windowActivated() and windowDeactivated()
      private static int menuState = FileStatus.NO_FILE;
     	
   	// PLEASE PUT THESE TWO (& THEIR METHODS) SOMEWHERE THEY BELONG, NOT HERE
      private static boolean reset= true; // registers/memory reset for execution
      private static boolean started = false;  // started execution
      Editor editor;
   	
   	// components of the menubar
      private JMenu file, run, window, help, edit, settings;
      private JMenuItem fileNew, fileOpen, fileClose, fileCloseAll, fileSave, fileSaveAs, fileSaveAll, fileDumpMemory, filePrint, fileExit;
      private JMenuItem editUndo, editRedo, editCut, editCopy, editPaste, editFindReplace, editSelectAll;
      private JMenuItem runGo, runStep, runBackstep, runReset, runAssemble, runStop, runPause, runClearBreakpoints, runToggleBreakpoints;
      private JCheckBoxMenuItem settingsLabel, settingsPopupInput, settingsValueDisplayBase, settingsAddressDisplayBase,
              settingsExtended, settingsAssembleOnOpen, settingsAssembleAll, settingsWarningsAreErrors, settingsStartAtMain,
      		  settingsDelayedBranching, settingsProgramArguments, settingsSelfModifyingCode;
      private JMenuItem settingsExceptionHandler, settingsEditor, settingsHighlighting, settingsMemoryConfiguration;
      private JMenuItem helpHelp, helpAbout;
      
      private JMenu fileOpenRecent;
      private RecentFiles recentFiles;

      // components of the toolbar
      private JButton Undo, Redo, Cut, Copy, Paste, FindReplace, SelectAll;
      private JButton New, Open, Save, SaveAs, SaveAll, DumpMemory, Print;
      private JButton Run, Assemble, Reset, Step, Backstep, Stop, Pause;
      private JButton Help;
   
      // The "action" objects, which include action listeners.  One of each will be created then
   	// shared between a menu item and its corresponding toolbar button.  This is a very cool
   	// technique because it relates the button and menu item so closely
   	
      private Action fileNewAction, fileOpenAction, fileCloseAction, fileCloseAllAction, fileSaveAction;
      private Action fileSaveAsAction, fileSaveAllAction, fileDumpMemoryAction, filePrintAction, fileExitAction;
      EditUndoAction editUndoAction;
      EditRedoAction editRedoAction;
      private Action editCutAction, editCopyAction, editPasteAction, editFindReplaceAction, editSelectAllAction;
      private Action runAssembleAction, runGoAction, runStepAction, runBackstepAction, runResetAction, 
                     runStopAction, runPauseAction, runClearBreakpointsAction, runToggleBreakpointsAction;
      private Action settingsLabelAction, settingsPopupInputAction, settingsValueDisplayBaseAction, settingsAddressDisplayBaseAction,
                     settingsExtendedAction, settingsAssembleOnOpenAction, settingsAssembleAllAction,
      					settingsWarningsAreErrorsAction, settingsStartAtMainAction, settingsProgramArgumentsAction,
      					settingsDelayedBranchingAction, settingsExceptionHandlerAction, settingsEditorAction,
      					settingsHighlightingAction, settingsMemoryConfigurationAction, settingsSelfModifyingCodeAction;    
      private Action helpHelpAction, helpAboutAction;
   
   
    /**
      *  Constructor for the Class. Sets up a window object for the UI
   	*   @param s Name of the window to be created.
   	**/     
   
       public VenusUI(String s) {
         super(s);
         mainUI = this;
         Globals.setGui(this);
         this.editor = new Editor(this);
      		 
         double screenWidth  = Toolkit.getDefaultToolkit().getScreenSize().getWidth();
         double screenHeight = Toolkit.getDefaultToolkit().getScreenSize().getHeight();
         // basically give up some screen space if running at 800 x 600
         double messageWidthPct = (screenWidth<1000.0)? 0.67 : 0.73;
         double messageHeightPct = (screenWidth<1000.0)? 0.12 : 0.15;
         double mainWidthPct = (screenWidth<1000.0)? 0.67 : 0.73;
         double mainHeightPct = (screenWidth<1000.0)? 0.60 : 0.65;
         double registersWidthPct = (screenWidth<1000.0)? 0.18 : 0.22;
         double registersHeightPct = (screenWidth<1000.0)? 0.72 : 0.80;
      				
         Dimension messagesPanePreferredSize = new Dimension((int)(screenWidth*messageWidthPct),(int)(screenHeight*messageHeightPct)); 
         Dimension mainPanePreferredSize = new Dimension((int)(screenWidth*mainWidthPct),(int)(screenHeight*mainHeightPct));
         Dimension registersPanePreferredSize = new Dimension((int)(screenWidth*registersWidthPct),(int)(screenHeight*registersHeightPct));
      	
         // the "restore" size (window control button that toggles with maximize)
      	// I want to keep it large, with enough room for user to get handles
         //this.setSize((int)(screenWidth*.8),(int)(screenHeight*.8));
      
         Globals.initialize(true);

         boolean macOS = new String("Mac OS X").equals( System.getProperty("os.name") );
               
         URL im;
         if ( macOS ) {
            im = this.getClass().getResource( Globals.imagesPath + "mars_minimize.png" );
         } else {
            //  image courtesy of NASA/JPL.  
            im = this.getClass().getResource( Globals.imagesPath + "RedMars16.gif");
         }
         
         if (im == null) {
            System.out.println("Internal Error: images folder or file not found");
            System.exit(0);
         }

         Image mars = Toolkit.getDefaultToolkit().getImage(im);
         this.setIconImage(mars);
      	// Everything in frame will be arranged on JPanel "center", which is only frame component.
      	// "center" has BorderLayout and 2 major components:
      	//   -- panel (jp) on North with 2 components
      	//      1. toolbar
      	//      2. run speed slider.
      	//   -- split pane (horizonSplitter) in center with 2 components side-by-side
      	//      1. split pane (splitter) with 2 components stacked
      	//         a. main pane, with 2 tabs (edit, execute)
      	//         b. messages pane with 2 tabs (mars, run I/O)
      	//      2. registers pane with 3 tabs (register file, coproc 0, coproc 1)
      	// I should probably run this breakdown out to full detail.  The components are created
      	// roughly in bottom-up order; some are created in component constructors and thus are
      	// not visible here.
      	
         registersTab = new RegistersWindow();
         coprocessor1Tab = new Coprocessor1Window();
         coprocessor0Tab = new Coprocessor0Window();
         registersPane = new RegistersPane(mainUI, registersTab,coprocessor1Tab, coprocessor0Tab);
         registersPane.setPreferredSize(registersPanePreferredSize);
      	
      	//Insets defaultTabInsets = (Insets)UIManager.get("TabbedPane.tabInsets");
      	//UIManager.put("TabbedPane.tabInsets", new Insets(1, 1, 1, 1));
         mainPane = new MainPane(mainUI, editor, registersTab, coprocessor1Tab, coprocessor0Tab);
      	//UIManager.put("TabbedPane.tabInsets", defaultTabInsets); 
      	
         mainPane.setPreferredSize(mainPanePreferredSize);
         messagesPane= new MessagesPane();
         messagesPane.setPreferredSize(messagesPanePreferredSize);
         splitter= new JSplitPane(JSplitPane.VERTICAL_SPLIT, mainPane, messagesPane);
         splitter.setOneTouchExpandable(true);
         splitter.resetToPreferredSizes();
         horizonSplitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, splitter, registersPane);
         horizonSplitter.setOneTouchExpandable(true);
         horizonSplitter.resetToPreferredSizes();
         
         // due to dependencies, do not set up menu/toolbar until now.
         this.createActionObjects();
         menu= this.setUpMenuBar();
         this.setJMenuBar(menu);
      	
         toolbar= this.setUpToolBar();
      
         JPanel jp = new JPanel(new FlowLayout(FlowLayout.LEFT));
         jp.add(toolbar);
         jp.add(RunSpeedPanel.getInstance());
         JPanel center= new JPanel(new BorderLayout());
         center.add(jp, BorderLayout.NORTH);
         center.add(horizonSplitter);

         this.getContentPane().add(center);
      
         FileStatus.reset();
      	// The following has side effect of establishing menu state
         FileStatus.set(FileStatus.NO_FILE);  

         // This is invoked when opening the app.  It will set the app to
         // appear at full screen size.
         if ( !macOS ) {
            this.addWindowListener(
                new WindowAdapter() {
                   public void windowOpened(WindowEvent e) {
                     mainUI.setExtendedState(JFrame.MAXIMIZED_BOTH); 
                  }
               });
         }
         
         // This is invoked when exiting the app through the X icon.  It will in turn
         // check for unsaved edits before exiting.
         this.addWindowListener(
                new WindowAdapter() {
                   public void windowClosing(WindowEvent e) {
                     if (mainUI.editor.closeAll()) {
                        System.exit(0);
                     } 
                  }
               });
      			
      	// The following will handle the windowClosing event properly in the 
      	// situation where user Cancels out of "save edits?" dialog.  By default,
      	// the GUI frame will be hidden but I want it to do nothing.
         this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
      
         this.pack();
         this.setVisible(true);
      }
   	
   	
    /*
     * Action objects are used instead of action listeners because one can be easily shared between
     * a menu item and a toolbar button.  Does nice things like disable both if the action is
     * disabled, etc.
     */
       private void createActionObjects() {
         // Toolkit tk = Toolkit.getDefaultToolkit();
         // Class<? extends VenusUI> cs = this.getClass(); 
         try {
            fileNewAction           = new FileNewAction( mainUI );
            fileOpenAction          = new FileOpenAction(mainUI);
            fileCloseAction         = new FileCloseAction( mainUI );
            fileCloseAllAction      = new FileCloseAllAction( mainUI );
            fileSaveAction          = new FileSaveAction( mainUI );
            fileSaveAsAction        = new FileSaveAsAction( mainUI );
            fileSaveAllAction       = new FileSaveAllAction( mainUI ); 
            fileDumpMemoryAction    = new FileDumpMemoryAction( mainUI );
            filePrintAction         = new FilePrintAction( mainUI );
            fileExitAction          = new FileExitAction( mainUI );	

            editUndoAction          = new EditUndoAction( mainUI );
            editRedoAction          = new EditRedoAction( mainUI );
            editCutAction           = new EditCutAction( mainUI );
            editCopyAction          = new EditCopyAction( mainUI );
            editPasteAction         = new EditPasteAction( mainUI );
            editFindReplaceAction   = new EditFindReplaceAction( mainUI );
            editSelectAllAction     = new EditSelectAllAction( mainUI );

            runAssembleAction       = new RunAssembleAction( mainUI );
            runGoAction             = new RunGoAction( mainUI );
            runStepAction           = new RunStepAction( mainUI );
            runBackstepAction       = new RunBackstepAction( mainUI );
            runPauseAction          = new RunPauseAction( mainUI );
            runStopAction           = new RunStopAction( mainUI );
            runResetAction          = new RunResetAction( mainUI );
            runClearBreakpointsAction = new RunClearBreakpointsAction( mainUI );
            runToggleBreakpointsAction = new RunToggleBreakpointsAction( mainUI );

            settingsLabelAction = new SettingsLabelAction(mainUI);
            settingsPopupInputAction = new SettingsPopupInputAction(mainUI);
            settingsValueDisplayBaseAction = new SettingsValueDisplayBaseAction(mainUI);
            settingsAddressDisplayBaseAction = new SettingsAddressDisplayBaseAction(mainUI);
            settingsExtendedAction = new SettingsExtendedAction(mainUI);
            settingsAssembleOnOpenAction = new SettingsAssembleOnOpenAction(mainUI);
            settingsAssembleAllAction = new SettingsAssembleAllAction(mainUI);
            settingsWarningsAreErrorsAction = new SettingsWarningsAreErrorsAction(mainUI);
            settingsStartAtMainAction = new SettingsStartAtMainAction(mainUI);
            settingsProgramArgumentsAction = new SettingsProgramArgumentsAction(mainUI);
            settingsDelayedBranchingAction = new SettingsDelayedBranchingAction(mainUI);
            settingsSelfModifyingCodeAction = new SettingsSelfModifyingCodeAction(mainUI);
            settingsEditorAction = new SettingsEditorAction(mainUI);
            settingsHighlightingAction = new SettingsHighlightingAction(mainUI);
            settingsExceptionHandlerAction = new SettingsExceptionHandlerAction(mainUI);
            settingsMemoryConfigurationAction = new SettingsMemoryConfigurationAction(mainUI);

            helpHelpAction = new HelpHelpAction( mainUI );
            helpAboutAction = new HelpAboutAction( mainUI );
         } 
             catch (NullPointerException e) {
               System.out.println("Internal Error: images folder not found, or other null pointer exception while creating Action objects");
               e.printStackTrace();
               System.exit(0);
            }
      }
   
    /*
     * build the menus and connect them to action objects (which serve as action listeners
     * shared between menu item and corresponding toolbar icon).
     */
    
       private JMenuBar setUpMenuBar() {
      
         Toolkit tk = Toolkit.getDefaultToolkit();
         Class<? extends VenusUI> cs = this.getClass(); 
         JMenuBar menuBar = new JMenuBar();
         file=new JMenu("File");
         file.setMnemonic(KeyEvent.VK_F);
         edit = new JMenu("Edit");
         edit.setMnemonic(KeyEvent.VK_E);
         run=new JMenu("Run");
         run.setMnemonic(KeyEvent.VK_R);
         //window = new JMenu("Window");
         //window.setMnemonic(KeyEvent.VK_W);
         settings = new JMenu("Settings");
         settings.setMnemonic(KeyEvent.VK_S);
         help = new JMenu("Help");
         help.setMnemonic(KeyEvent.VK_H); 
      	// slight bug: user typing alt-H activates help menu item directly, not help menu
         
         // Don't use menu icons or File Exit menu item in Mac OS X
         boolean macOS = new String("Mac OS X").equals( System.getProperty("os.name") );

         boolean addFileExit = true;
         if ( macOS ) {
            addFileExit = false;
         };

         recentFiles = new RecentFiles(mainUI);

         fileNew = new JMenuItem(fileNewAction);
         fileOpen = new JMenuItem(fileOpenAction);

         fileClose = new JMenuItem(fileCloseAction);
         fileCloseAll = new JMenuItem(fileCloseAllAction);
         fileSave = new JMenuItem(fileSaveAction);
         fileSaveAs = new JMenuItem(fileSaveAsAction);
         fileSaveAll = new JMenuItem(fileSaveAllAction);
         fileDumpMemory = new JMenuItem(fileDumpMemoryAction);
         filePrint = new JMenuItem(filePrintAction);
         fileExit = new JMenuItem(fileExitAction);

         /*
         if ( useIcons ) {
            fileNew.setIcon(new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath+"New16.png"))));
            fileOpen.setIcon(new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath+"Open16.png"))));
            fileClose.setIcon(new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath+"MyBlank16.gif"))));
            fileCloseAll.setIcon(new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath+"MyBlank16.gif"))));
            fileSave.setIcon(new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath+"Save16.png"))));
            fileSaveAs.setIcon(new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath+"SaveAs16.png"))));
            fileSaveAll.setIcon(new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath+"MyBlank16.gif"))));
            fileDumpMemory.setIcon(new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath+"Dump16.png"))));
            filePrint.setIcon(new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath+"Print16.gif"))));
            fileExit.setIcon(new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath+"MyBlank16.gif"))));
         } else {
            fileNew.setIcon(null);
            fileOpen.setIcon(null);
            fileClose.setIcon(null);
            fileCloseAll.setIcon(null);
            fileSave.setIcon(null);
            fileSaveAs.setIcon(null);
            fileSaveAll.setIcon(null);
            fileDumpMemory.setIcon(null);
            filePrint.setIcon(null);
            fileExit.setIcon(null);
         }
         */
         
         file.add(fileNew);
         file.add(fileOpen);

         file.add( recentFiles.getMenu() );

         file.add(fileClose);
         file.add(fileCloseAll);
         file.addSeparator();
         file.add(fileSave);
         file.add(fileSaveAs);
         file.add(fileSaveAll);
         if (new mars.mips.dump.DumpFormatLoader().loadDumpFormats().size() > 0) {
            file.add(fileDumpMemory);
         }
         file.addSeparator();
         file.add(filePrint);

         if ( addFileExit ) {
            file.addSeparator();
            file.add(fileExit);
         }
      	
         editUndo = new JMenuItem(editUndoAction);
         editRedo = new JMenuItem(editRedoAction);
         editCut = new JMenuItem(editCutAction);
         editCopy = new JMenuItem(editCopyAction);
         editPaste = new JMenuItem(editPasteAction);
         editFindReplace = new JMenuItem(editFindReplaceAction);
         editSelectAll = new JMenuItem(editSelectAllAction);

         /*
         if ( useIcons ) {
            editUndo.setIcon(new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath+"Undo16.png"))));//"Undo16.gif"))));
            editRedo.setIcon(new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath+"Redo16.png"))));//"Redo16.gif"))));      
            editCut.setIcon(new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath+"Cut16.gif"))));
            editCopy.setIcon(new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath+"Copy16.png"))));//"Copy16.gif"))));
            editPaste.setIcon(new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath+"Paste16.png"))));//"Paste16.gif"))));
            editFindReplace.setIcon(new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath+"Find16.png"))));//"Paste16.gif"))));
            editSelectAll.setIcon(new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath+"MyBlank16.gif"))));
         } else {
            editUndo.setIcon(null);
            editRedo.setIcon(null);
            editCut.setIcon(null);
            editCopy.setIcon(null);
            editPaste.setIcon(null);
            editFindReplace.setIcon(null);
            editSelectAll.setIcon(null);
         }
            */

         edit.add(editUndo);
         edit.add(editRedo);
         edit.addSeparator();
         edit.add(editCut);
         edit.add(editCopy);
         edit.add(editPaste);
         edit.addSeparator();
         edit.add(editFindReplace);
         edit.add(editSelectAll);
      
         runAssemble = new JMenuItem(runAssembleAction);
         runGo = new JMenuItem(runGoAction);
         runStep = new JMenuItem(runStepAction);
         runBackstep = new JMenuItem(runBackstepAction);
         runReset = new JMenuItem(runResetAction);
         runStop = new JMenuItem(runStopAction);
         runPause = new JMenuItem(runPauseAction);
         runClearBreakpoints = new JMenuItem(runClearBreakpointsAction);
         runToggleBreakpoints = new JMenuItem(runToggleBreakpointsAction);
         
         /*
         if ( useIcons ) {
            runAssemble.setIcon(new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath+"Assemble16.png"))));//"MyAssemble16.gif"))));
            runGo.setIcon(new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath+"Play16.png"))));//"Play16.gif"))));
            runStep.setIcon(new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath+"StepForward16.png"))));//"MyStepForward16.gif"))));
            runBackstep.setIcon(new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath+"StepBack16.png"))));//"MyStepBack16.gif"))));
            runReset.setIcon(new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath+"Reset16.png"))));//"MyReset16.gif"))));
            runStop.setIcon(new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath+"Stop16.png"))));//"Stop16.gif"))));
            runPause.setIcon(new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath+"Pause16.png"))));//"Pause16.gif"))));
            runClearBreakpoints.setIcon(new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath+"MyBlank16.gif"))));
            runToggleBreakpoints.setIcon(new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath+"MyBlank16.gif"))));
         } else {
            runAssemble.setIcon(null);
            runGo.setIcon(null);
            runStep.setIcon(null);
            runBackstep.setIcon(null);
            runReset.setIcon(null);
            runStop.setIcon(null);
            runPause.setIcon(null);
            runClearBreakpoints.setIcon(null);
            runToggleBreakpoints.setIcon(null);
         }
            */

         run.add(runAssemble);
         run.add(runGo);
         run.add(runStep);
         run.add(runBackstep);
         run.add(runPause);
         run.add(runStop);
         run.add(runReset);
         run.addSeparator();
         run.add(runClearBreakpoints);
         run.add(runToggleBreakpoints);
      	
         settingsLabel = new JCheckBoxMenuItem(settingsLabelAction);
         settingsLabel.setSelected(Globals.getSettings().getBooleanSetting(Settings.LABEL_WINDOW_VISIBILITY));

         settingsPopupInput = new JCheckBoxMenuItem(settingsPopupInputAction);
         settingsPopupInput.setSelected(Globals.getSettings().getBooleanSetting(Settings.POPUP_SYSCALL_INPUT));

         settingsValueDisplayBase = new JCheckBoxMenuItem(settingsValueDisplayBaseAction);
         settingsValueDisplayBase.setSelected(Globals.getSettings().getBooleanSetting(Settings.DISPLAY_VALUES_IN_HEX));

         // Tell the corresponding JCheckBox in the Execute Pane about me -- it has already been created.
         mainPane.getExecutePane().getValueDisplayBaseChooser().setSettingsMenuItem(settingsValueDisplayBase);
         settingsAddressDisplayBase = new JCheckBoxMenuItem(settingsAddressDisplayBaseAction);
         settingsAddressDisplayBase.setSelected(Globals.getSettings().getBooleanSetting(Settings.DISPLAY_ADDRESSES_IN_HEX));
         

         // Tell the corresponding JCheckBox in the Execute Pane about me -- it has already been created.
         mainPane.getExecutePane().getAddressDisplayBaseChooser().setSettingsMenuItem(settingsAddressDisplayBase);
         settingsExtended = new JCheckBoxMenuItem(settingsExtendedAction);
         settingsExtended.setSelected(Globals.getSettings().getBooleanSetting(Settings.EXTENDED_ASSEMBLER_ENABLED));

         settingsDelayedBranching = new JCheckBoxMenuItem(settingsDelayedBranchingAction);
         settingsDelayedBranching.setSelected(Globals.getSettings().getBooleanSetting(Settings.DELAYED_BRANCHING_ENABLED));

         settingsSelfModifyingCode = new JCheckBoxMenuItem(settingsSelfModifyingCodeAction);
         settingsSelfModifyingCode.setSelected(Globals.getSettings().getBooleanSetting(Settings.SELF_MODIFYING_CODE_ENABLED));

         settingsAssembleOnOpen = new JCheckBoxMenuItem(settingsAssembleOnOpenAction);
         settingsAssembleOnOpen.setSelected(Globals.getSettings().getBooleanSetting(Settings.ASSEMBLE_ON_OPEN_ENABLED));

         settingsAssembleAll = new JCheckBoxMenuItem(settingsAssembleAllAction);
         settingsAssembleAll.setSelected(Globals.getSettings().getBooleanSetting(Settings.ASSEMBLE_ALL_ENABLED));

         settingsWarningsAreErrors = new JCheckBoxMenuItem(settingsWarningsAreErrorsAction);
         settingsWarningsAreErrors.setSelected(Globals.getSettings().getBooleanSetting(Settings.WARNINGS_ARE_ERRORS));

         settingsStartAtMain = new JCheckBoxMenuItem(settingsStartAtMainAction);
         settingsStartAtMain.setSelected(Globals.getSettings().getBooleanSetting(Settings.START_AT_MAIN));

         settingsProgramArguments = new JCheckBoxMenuItem(settingsProgramArgumentsAction);
         settingsProgramArguments.setSelected(Globals.getSettings().getBooleanSetting(Settings.PROGRAM_ARGUMENTS));


         settingsEditor = new JMenuItem(settingsEditorAction);
         settingsHighlighting = new JMenuItem(settingsHighlightingAction);
         settingsExceptionHandler = new JMenuItem(settingsExceptionHandlerAction);
         settingsMemoryConfiguration = new JMenuItem(settingsMemoryConfigurationAction);
      	
         settings.add(settingsLabel);
         settings.add(settingsProgramArguments);
         settings.add(settingsPopupInput);
         settings.add(settingsAddressDisplayBase);
         settings.add(settingsValueDisplayBase);
         settings.addSeparator();
         settings.add(settingsAssembleOnOpen);
         settings.add(settingsAssembleAll);
         settings.add(settingsWarningsAreErrors);
         settings.add(settingsStartAtMain);
         settings.addSeparator();
         settings.add(settingsExtended);
         settings.add(settingsDelayedBranching);
         settings.add(settingsSelfModifyingCode);
         settings.addSeparator();
         settings.add(settingsEditor);
         settings.add(settingsHighlighting);
         settings.add(settingsExceptionHandler);
         settings.add(settingsMemoryConfiguration);

         helpHelp = new JMenuItem(helpHelpAction);
         helpAbout = new JMenuItem(helpAboutAction);
         /*
         if ( useIcons ) {
            helpHelp.setIcon(new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath+"Help16.png"))));//"Help16.gif"))));
            helpAbout.setIcon(new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath+"MyBlank16.gif"))));
         } else {
            helpHelp.setIcon(null);
            helpAbout.setIcon(null);
         }
            */

         help.add(helpHelp);
         help.addSeparator();
         help.add(helpAbout);
      
         menuBar.add(file);
         menuBar.add(edit);
         menuBar.add(run);
         menuBar.add(settings);
         JMenu toolMenu = new ToolLoader().buildToolsMenu();
         if (toolMenu != null) menuBar.add(toolMenu);
         menuBar.add(help);
      	
      	// experiment with popup menu for settings. 3 Aug 2006 PS
         //setupPopupMenu();
      	
         return menuBar;
      }
   
    /*
     * build the toolbar and connect items to action objects (which serve as action listeners
     * shared between toolbar icon and corresponding menu item).
     */
   
       JToolBar setUpToolBar() {
         JToolBar toolBar = new JToolBar();
      	
         New = new JButton(fileNewAction);
         New.setText("");
         New.setIcon( multiResolutionIcon("New") );

         Open = new JButton(fileOpenAction);
         Open.setText(""); 
         Open.setIcon( multiResolutionIcon("Open") );

         Save = new JButton(fileSaveAction);
         Save.setText("");
         Save.setIcon(multiResolutionIcon("Save"));

         SaveAs = new JButton(fileSaveAsAction);
         SaveAs.setText("");
         SaveAs.setIcon(multiResolutionIcon("SaveAs"));


         DumpMemory = new JButton(fileDumpMemoryAction);
         DumpMemory.setText("");
         DumpMemory.setIcon(multiResolutionIcon("Dump"));

         Print= new JButton(filePrintAction);
         Print.setText("");
         Print.setIcon(multiResolutionIcon("Print"));
      
         Undo = new JButton(editUndoAction);
         Undo.setText(""); 
         Undo.setIcon(multiResolutionIcon("Undo"));

         Redo = new JButton(editRedoAction);
         Redo.setText("");   	
         Redo.setIcon(multiResolutionIcon("Redo"));
         
         Cut= new JButton(editCutAction);
         Cut.setText("");
         Cut.setIcon(multiResolutionIcon("Cut"));

         Copy= new JButton(editCopyAction);
         Copy.setText("");
         Copy.setIcon(multiResolutionIcon("Copy"));

         Paste= new JButton(editPasteAction);
         Paste.setText("");
         Paste.setIcon(multiResolutionIcon("Paste"));

         FindReplace = new JButton(editFindReplaceAction);
         FindReplace.setText("");
         FindReplace.setIcon(multiResolutionIcon("Find"));

         // SelectAll = new JButton(editSelectAllAction);
         // SelectAll.setText("");
      	
         Run = new JButton(runGoAction);
         Run.setText("");
         Run.setIcon(multiResolutionIcon("Play"));

         Assemble = new JButton(runAssembleAction);
         Assemble.setText("");
         Assemble.setIcon( multiResolutionIcon("Assemble") );

         Step = new JButton(runStepAction);
         Step.setText(""); 
         Step.setIcon( multiResolutionIcon("StepForward") );

         Backstep = new JButton(runBackstepAction);
         Backstep.setText("");
         Backstep.setIcon( multiResolutionIcon("StepBack") );

         Reset = new JButton(runResetAction);
         Reset.setText("");
         Reset.setIcon(multiResolutionIcon("Reset"));

         Stop = new JButton(runStopAction);
         Stop.setText("");
         Stop.setIcon(multiResolutionIcon("Stop"));
         
         Pause = new JButton(runPauseAction);
         Pause.setText("");
         Pause.setIcon(multiResolutionIcon("Pause"));
         
         Help= new JButton(helpHelpAction);
         Help.setText("");
         
         toolBar.add(New);
         toolBar.add(Open);
         toolBar.add(Save);
         toolBar.add(SaveAs);
         if (new mars.mips.dump.DumpFormatLoader().loadDumpFormats().size() > 0) {
            toolBar.add(DumpMemory);
         }
         toolBar.add(Print);
         toolBar.add(new JToolBar.Separator());
         toolBar.add(Undo);
         toolBar.add(Redo);
         toolBar.add(Cut);
         toolBar.add(Copy);
         toolBar.add(Paste);
         toolBar.add(FindReplace);
         toolBar.add(new JToolBar.Separator());
         toolBar.add(Assemble);
         toolBar.add(Run);   
         toolBar.add(Step);
         toolBar.add(Backstep);
         toolBar.add(Pause);
         toolBar.add(Stop);
         toolBar.add(Reset);
         toolBar.add(new JToolBar.Separator());
         toolBar.add(Help);
         toolBar.add(new JToolBar.Separator());
      	
         return toolBar;
      }
      
   	
    /* Determine from FileStatus what the menu state (enabled/disabled)should 
     * be then call the appropriate method to set it.  Current states are:
     *
     * setMenuStateInitial: set upon startup and after File->Close
     * setMenuStateEditingNew: set upon File->New
     * setMenuStateEditing: set upon File->Open or File->Save or erroneous Run->Assemble
     * setMenuStateRunnable: set upon successful Run->Assemble
     * setMenuStateRunning: set upon Run->Go
     * setMenuStateTerminated: set upon completion of simulated execution
     */
       void setMenuState(int status) {
         menuState = status; 
         switch (status) {
            case FileStatus.NO_FILE:
               setMenuStateInitial();
               break;
            case FileStatus.NEW_NOT_EDITED:
               setMenuStateEditingNew();
               break;
            case FileStatus.NEW_EDITED:
               setMenuStateEditingNew();
               break;
            case FileStatus.NOT_EDITED:
               setMenuStateNotEdited(); // was MenuStateEditing. DPS 9-Aug-2011
               break;
            case FileStatus.EDITED:
               setMenuStateEditing();
               break;
            case FileStatus.RUNNABLE:
               setMenuStateRunnable();
               break;
            case FileStatus.RUNNING:
               setMenuStateRunning();
               break;
            case FileStatus.TERMINATED:
               setMenuStateTerminated();
               break;
            case FileStatus.OPENING:// This is a temporary state. DPS 9-Aug-2011
               break;
            default:
               System.out.println("Invalid File Status: "+status);
               break;
         }
      }
     
     
       void setMenuStateInitial() {
         fileNewAction.setEnabled(true);
         fileOpenAction.setEnabled(true);
         fileCloseAction.setEnabled(false);
         fileCloseAllAction.setEnabled(false);
         fileSaveAction.setEnabled(false);
         fileSaveAsAction.setEnabled(false);
         fileSaveAllAction.setEnabled(false);
         fileDumpMemoryAction.setEnabled(false);
         filePrintAction.setEnabled(false);
         fileExitAction.setEnabled(true);
         editUndoAction.setEnabled(false);
         editRedoAction.setEnabled(false);
         editCutAction.setEnabled(false);
         editCopyAction.setEnabled(false);
         editPasteAction.setEnabled(false);
         editFindReplaceAction.setEnabled(false);
         editSelectAllAction.setEnabled(false);
         settingsDelayedBranchingAction.setEnabled(true); // added 25 June 2007
         settingsMemoryConfigurationAction.setEnabled(true); // added 21 July 2009
         runAssembleAction.setEnabled(false);
         runGoAction.setEnabled(false);
         runStepAction.setEnabled(false);
         runBackstepAction.setEnabled(false);
         runResetAction.setEnabled(false);
         runStopAction.setEnabled(false);
         runPauseAction.setEnabled(false);
         runClearBreakpointsAction.setEnabled(false);
         runToggleBreakpointsAction.setEnabled(false);
         helpHelpAction.setEnabled(true);
         helpAboutAction.setEnabled(true);
         editUndoAction.updateUndoState();
         editRedoAction.updateRedoState();
      }
   
      /* Added DPS 9-Aug-2011, for newly-opened files.  Retain
   	   existing Run menu state (except Assemble, which is always true).
   		Thus if there was a valid assembly it is retained. */
       void setMenuStateNotEdited() {
      /* Note: undo and redo are handled separately by the undo manager*/  
         fileNewAction.setEnabled(true);
         fileOpenAction.setEnabled(true);
         fileCloseAction.setEnabled(true);
         fileCloseAllAction.setEnabled(true);
         fileSaveAction.setEnabled(true);
         fileSaveAsAction.setEnabled(true);
         fileSaveAllAction.setEnabled(true);
         fileDumpMemoryAction.setEnabled(false);
         filePrintAction.setEnabled(true);
         fileExitAction.setEnabled(true);
         editCutAction.setEnabled(true);
         editCopyAction.setEnabled(true);
         editPasteAction.setEnabled(true);
         editFindReplaceAction.setEnabled(true);
         editSelectAllAction.setEnabled(true);
         settingsDelayedBranchingAction.setEnabled(true); 
         settingsMemoryConfigurationAction.setEnabled(true);
         runAssembleAction.setEnabled(true);
			// If assemble-all, allow previous Run menu settings to remain.
			// Otherwise, clear them out.  DPS 9-Aug-2011
         if (!Globals.getSettings().getBooleanSetting(mars.Settings.ASSEMBLE_ALL_ENABLED)) {
            runGoAction.setEnabled(false);
            runStepAction.setEnabled(false);
            runBackstepAction.setEnabled(false);
            runResetAction.setEnabled(false);
            runStopAction.setEnabled(false);
            runPauseAction.setEnabled(false);
            runClearBreakpointsAction.setEnabled(false);
            runToggleBreakpointsAction.setEnabled(false);
         } 
         helpHelpAction.setEnabled(true);
         helpAboutAction.setEnabled(true);
         editUndoAction.updateUndoState();
         editRedoAction.updateRedoState();
      }
   
   
   
   
       void setMenuStateEditing() {
      /* Note: undo and redo are handled separately by the undo manager*/  
         fileNewAction.setEnabled(true);
         fileOpenAction.setEnabled(true);
         fileCloseAction.setEnabled(true);
         fileCloseAllAction.setEnabled(true);
         fileSaveAction.setEnabled(true);
         fileSaveAsAction.setEnabled(true);
         fileSaveAllAction.setEnabled(true);
         fileDumpMemoryAction.setEnabled(false);
         filePrintAction.setEnabled(true);
         fileExitAction.setEnabled(true);
         editCutAction.setEnabled(true);
         editCopyAction.setEnabled(true);
         editPasteAction.setEnabled(true);
         editFindReplaceAction.setEnabled(true);
         editSelectAllAction.setEnabled(true);
         settingsDelayedBranchingAction.setEnabled(true); // added 25 June 2007
         settingsMemoryConfigurationAction.setEnabled(true); // added 21 July 2009
         runAssembleAction.setEnabled(true);
         runGoAction.setEnabled(false);
         runStepAction.setEnabled(false);
         runBackstepAction.setEnabled(false);
         runResetAction.setEnabled(false);
         runStopAction.setEnabled(false);
         runPauseAction.setEnabled(false);
         runClearBreakpointsAction.setEnabled(false);
         runToggleBreakpointsAction.setEnabled(false);
         helpHelpAction.setEnabled(true);
         helpAboutAction.setEnabled(true);
         editUndoAction.updateUndoState();
         editRedoAction.updateRedoState();
      }
   
     /* Use this when "File -> New" is used
      */
       void setMenuStateEditingNew() {
      /* Note: undo and redo are handled separately by the undo manager*/  
         fileNewAction.setEnabled(true);
         fileOpenAction.setEnabled(true);
         fileCloseAction.setEnabled(true);
         fileCloseAllAction.setEnabled(true);
         fileSaveAction.setEnabled(true);
         fileSaveAsAction.setEnabled(true);
         fileSaveAllAction.setEnabled(true);
         fileDumpMemoryAction.setEnabled(false);
         filePrintAction.setEnabled(true);
         fileExitAction.setEnabled(true);
         editCutAction.setEnabled(true);
         editCopyAction.setEnabled(true);
         editPasteAction.setEnabled(true);
         editFindReplaceAction.setEnabled(true);
         editSelectAllAction.setEnabled(true);
         settingsDelayedBranchingAction.setEnabled(true); // added 25 June 2007
         settingsMemoryConfigurationAction.setEnabled(true); // added 21 July 2009
         runAssembleAction.setEnabled(false);
         runGoAction.setEnabled(false);
         runStepAction.setEnabled(false);
         runBackstepAction.setEnabled(false);
         runResetAction.setEnabled(false);
         runStopAction.setEnabled(false);
         runPauseAction.setEnabled(false);
         runClearBreakpointsAction.setEnabled(false);
         runToggleBreakpointsAction.setEnabled(false);
         helpHelpAction.setEnabled(true);
         helpAboutAction.setEnabled(true);
         editUndoAction.updateUndoState();
         editRedoAction.updateRedoState();
      }
    	 
     /* Use this upon successful assemble or reset
      */
       void setMenuStateRunnable() {
      /* Note: undo and redo are handled separately by the undo manager */  
         fileNewAction.setEnabled(true);
         fileOpenAction.setEnabled(true);
         fileCloseAction.setEnabled(true);
         fileCloseAllAction.setEnabled(true);
         fileSaveAction.setEnabled(true);
         fileSaveAsAction.setEnabled(true);
         fileSaveAllAction.setEnabled(true);
         fileDumpMemoryAction.setEnabled(true);
         filePrintAction.setEnabled(true);
         fileExitAction.setEnabled(true);
         editCutAction.setEnabled(true);
         editCopyAction.setEnabled(true);
         editPasteAction.setEnabled(true);
         editFindReplaceAction.setEnabled(true);
         editSelectAllAction.setEnabled(true);
         settingsDelayedBranchingAction.setEnabled(true); // added 25 June 2007
         settingsMemoryConfigurationAction.setEnabled(true); // added 21 July 2009
         runAssembleAction.setEnabled(true);
         runGoAction.setEnabled(true);
         runStepAction.setEnabled(true);
         runBackstepAction.setEnabled(
            (Globals.getSettings().getBackSteppingEnabled()&& !Globals.program.getBackStepper().empty())
             ? true : false);
         runResetAction.setEnabled(true);
         runStopAction.setEnabled(false);
         runPauseAction.setEnabled(false);
         runToggleBreakpointsAction.setEnabled(true);
         helpHelpAction.setEnabled(true);
         helpAboutAction.setEnabled(true);
         editUndoAction.updateUndoState();
         editRedoAction.updateRedoState();
      }
   
     /* Use this while program is running
      */
       void setMenuStateRunning() {
      /* Note: undo and redo are handled separately by the undo manager */  
         fileNewAction.setEnabled(false);
         fileOpenAction.setEnabled(false);
         fileCloseAction.setEnabled(false);
         fileCloseAllAction.setEnabled(false);
         fileSaveAction.setEnabled(false);
         fileSaveAsAction.setEnabled(false);
         fileSaveAllAction.setEnabled(false);
         fileDumpMemoryAction.setEnabled(false);
         filePrintAction.setEnabled(false);
         fileExitAction.setEnabled(false);
         editCutAction.setEnabled(false);
         editCopyAction.setEnabled(false);
         editPasteAction.setEnabled(false);
         editFindReplaceAction.setEnabled(false);
         editSelectAllAction.setEnabled(false);
         settingsDelayedBranchingAction.setEnabled(false); // added 25 June 2007
         settingsMemoryConfigurationAction.setEnabled(false); // added 21 July 2009
         runAssembleAction.setEnabled(false);
         runGoAction.setEnabled(false);
         runStepAction.setEnabled(false);
         runBackstepAction.setEnabled(false);
         runResetAction.setEnabled(false);
         runStopAction.setEnabled(true);
         runPauseAction.setEnabled(true);
         runToggleBreakpointsAction.setEnabled(false);
         helpHelpAction.setEnabled(true);
         helpAboutAction.setEnabled(true);
         editUndoAction.setEnabled(false);//updateUndoState(); // DPS 10 Jan 2008
         editRedoAction.setEnabled(false);//updateRedoState(); // DPS 10 Jan 2008
      }   
     /* Use this upon completion of execution
      */
       void setMenuStateTerminated() {
      /* Note: undo and redo are handled separately by the undo manager */  
         fileNewAction.setEnabled(true);
         fileOpenAction.setEnabled(true);
         fileCloseAction.setEnabled(true);
         fileCloseAllAction.setEnabled(true);
         fileSaveAction.setEnabled(true);
         fileSaveAsAction.setEnabled(true);
         fileSaveAllAction.setEnabled(true);
         fileDumpMemoryAction.setEnabled(true);
         filePrintAction.setEnabled(true);
         fileExitAction.setEnabled(true);
         editCutAction.setEnabled(true);
         editCopyAction.setEnabled(true);
         editPasteAction.setEnabled(true);
         editFindReplaceAction.setEnabled(true);
         editSelectAllAction.setEnabled(true);
         settingsDelayedBranchingAction.setEnabled(true); // added 25 June 2007
         settingsMemoryConfigurationAction.setEnabled(true); // added 21 July 2009
         runAssembleAction.setEnabled(true);
         runGoAction.setEnabled(false);
         runStepAction.setEnabled(false);
         runBackstepAction.setEnabled(
            (Globals.getSettings().getBackSteppingEnabled()&& !Globals.program.getBackStepper().empty())
             ? true : false);
         runResetAction.setEnabled(true);
         runStopAction.setEnabled(false);
         runPauseAction.setEnabled(false);
         runToggleBreakpointsAction.setEnabled(true);
         helpHelpAction.setEnabled(true);
         helpAboutAction.setEnabled(true);
         editUndoAction.updateUndoState();
         editRedoAction.updateRedoState();
      }
   
    
    /**
     * Get current menu state.  State values are constants in FileStatus class.  DPS 23 July 2008
     * @return current menu state.
     **/
     
       public static int getMenuState() {
         return menuState;
      }
      
   	/**
   	  *  To set whether the register values are reset.
   	  *   @param b Boolean true if the register values have been reset.
   	  **/
   	
       public static void setReset(boolean b){
         reset=b;
      }
   
   	/**
   	  *  To set whether MIPS program execution has started.
   	  *   @param b true if the MIPS program execution has started.
   	  **/
   	
       public static void setStarted(boolean b){ 
         started=b;
      }
      /**
   	  *  To find out whether the register values are reset.
   	  *   @return Boolean true if the register values have been reset.
   	  **/
      
       public static boolean getReset(){
         return reset;
      }
   	
      /**
   	  *  To find out whether MIPS program is currently executing.
   	  *   @return  true if MIPS program is currently executing.
   	  **/
       public static boolean getStarted(){
         return started;
      }
   	
      /**
   	  *  Get reference to Editor object associated with this GUI.
   	  *   @return Editor for the GUI.
   	  **/
         	
       public Editor getEditor() {
         return editor;
      }		
   	
      /**
   	  *  Get reference to messages pane associated with this GUI.
   	  *   @return MessagesPane object associated with the GUI.
   	  **/
         	
       public MainPane getMainPane() {
         return mainPane;
      }      /**
   	  *  Get reference to messages pane associated with this GUI.
   	  *   @return MessagesPane object associated with the GUI.
   	  **/
         	
       public MessagesPane getMessagesPane() {
         return messagesPane;
      }
   
      /**
   	  *  Get reference to registers pane associated with this GUI.
   	  *   @return RegistersPane object associated with the GUI.
   	  **/
         	
       public RegistersPane getRegistersPane() {
         return registersPane;
      }   	
   
      /**
   	  *  Get reference to settings menu item for display base of memory/register values.
   	  *   @return the menu item
   	  **/
         	
       public JCheckBoxMenuItem getValueDisplayBaseMenuItem() {
         return settingsValueDisplayBase;
      }   	     
   
      /**
   	  *  Get reference to settings menu item for display base of memory/register values.
   	  *   @return the menu item
   	  **/
         	
       public JCheckBoxMenuItem getAddressDisplayBaseMenuItem() {
         return settingsAddressDisplayBase;
      }   	          
   	
   	/**
   	 * Return reference tothe Run->Assemble item's action.  Needed by File->Open in case
   	 * assemble-upon-open flag is set.
   	 * @return the Action object for the Run->Assemble operation.
   	 */
       public Action getRunAssembleAction() {
         return runAssembleAction;
      }
   	
   	/**
   	 * Have the menu request keyboard focus.  DPS 5-4-10
   	 */
       public void haveMenuRequestFocus() {
         this.menu.requestFocus();
      }
   	
   	/**
   	 * Send keyboard event to menu for possible processing.  DPS 5-4-10
   	 * @param evt KeyEvent for menu component to consider for processing.
   	 */
       public void dispatchEventToMenu(KeyEvent evt) {
         this.menu.dispatchEvent(evt);
      }
     
     // pop up menu experiment 3 Aug 2006.  Keep for possible later revival.
       private void setupPopupMenu() {
         JPopupMenu popup; 
         popup = new JPopupMenu();
      	// cannot put the same menu item object on two different menus.
      	// If you want to duplicate functionality, need a different item.
      	// Should be able to share listeners, but if both menu items are
      	// JCheckBoxMenuItem, how to keep their checked status in synch?
      	// If you popup this menu and check the box, the right action occurs
      	// but its counterpart on the regular menu is not checked.
         popup.add(new JCheckBoxMenuItem(settingsLabelAction)); 
      //Add listener to components that can bring up popup menus. 
         MouseListener popupListener = new PopupListener(popup); 
         this.addMouseListener(popupListener); 
      }
     
      /**
       * Adds file to the recent files list
       * 
       * @param filename
       */
      public void addRecentFile( String file ) {
         recentFiles.add( file );
      }
   
      /**
       * Gets the path of the most recent file
       * 
       * @return path
       */
      public String getRecentPath() {
         return recentFiles.getPath();
      }
   
      /**
       * Create a multi-resolution ImageIcon from a set of .png files for use
       * HiDPI monitors
       * 
       * @param basename   The base name for the icon. The routine expects to find
       *                   "basename22.png", "basename44.png" and "basename88.png"
       * @return           A multi-resolution ImageIcon
       */
      public ImageIcon multiResolutionIcon( String basename ) {
         
         // Generate list of files to read
         List<String> imgFiles = List.of(
            Globals.imagesPath+ basename + "22.png",
            Globals.imagesPath+ basename + "44.png",
            Globals.imagesPath+ basename + "88.png"
         );

         Toolkit tk = Toolkit.getDefaultToolkit();

         // Load images
         List<Image> images = new ArrayList<Image>();
         for (String url : imgFiles) {
            try {
               // We need to use this method because we may be reading
               // from the jar file
               images.add( tk.getImage(this.getClass().getResource(url)) );
            }
            catch (Exception e) {
               System.out.println("Unable to read image " + url );
               return null;
            }
         }

         // Create a MultiResolutionImage from the images
         BaseMultiResolutionImage multiResolutionImage = 
            new BaseMultiResolutionImage(images.toArray(new Image[0]));
         
         // Create an ImageIcon from them
         return new ImageIcon( multiResolutionImage );
      }
   }