package se.cambio.cds.view.swing.dialogs;

import se.cambio.cds.util.Domains;
import se.cambio.cds.view.swing.applicationobjects.DomainsUI;
import se.cambio.cm.model.util.CMElement;
import se.cambio.openehr.controller.session.data.ArchetypeManager;
import se.cambio.openehr.controller.session.data.Archetypes;
import se.cambio.openehr.controller.session.data.Templates;
import se.cambio.openehr.util.*;
import se.cambio.openehr.util.exceptions.InstanceNotFoundException;
import se.cambio.openehr.util.exceptions.InternalErrorException;
import se.cambio.openehr.view.panels.SelectionPanel;
import se.cambio.openehr.view.trees.SelectableNode;
import se.cambio.openehr.view.trees.SelectableNodeBuilder;
import se.cambio.openehr.view.util.ImportUtils;
import se.cambio.openehr.view.util.NodeConversor;
import se.cambio.openehr.view.util.ScreenUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Collection;


public class DialogArchetypeChooser extends JDialog{

    private static final long serialVersionUID = 1L;
    private JButton acceptButton;
    private JButton cancelButton;
    private JPanel jPanel;
    private JPanel bottonPanel;
    private boolean _answer = false;
    private AcceptChangesAction acceptChangesAction;
    private CancelChangesAction cancelChangesAction;
    private SelectionPanel archetypeSelectionPanel;
    private SelectionPanel templateSelectionPanel;
    private SelectableNode<String> archetypeNode = null;
    private SelectableNode<String> templateNode = null;
    private JComboBox domainComboBox;
    private JTabbedPane tabbedPane;
    private JCheckBox lastCB;

    private String ANY_DOMAIN = "*";

    public DialogArchetypeChooser(Window owner){
        super(owner,
                OpenEHRLanguageManager.getMessage("Archetypes") + "/" + OpenEHRLanguageManager.getMessage("Templates"),
                ModalityType.APPLICATION_MODAL);
        init(new Dimension(500,500), false);
    }


    public DialogArchetypeChooser(Window owner, String archetypeId, String domainId,boolean selectTemplates, boolean onlyShowCDS){
        super(
                owner,
                OpenEHRLanguageManager.getMessage("Archetypes") + "/" + OpenEHRLanguageManager.getMessage("Templates"),
                ModalityType.APPLICATION_MODAL);
        if (onlyShowCDS){
            getComboBox().setSelectedItem(Domains.CDS_ID);
            getComboBox().setEnabled(false);
        }else if (archetypeId!=null){
            if (domainId==null){
                domainId = ANY_DOMAIN;
            }
            if (!Domains.EHR_ID.equals(domainId)){//Select only if it is not EHR (the one by default) because it will activate the 'Last' ComboBox
                getComboBox().setSelectedItem(domainId);
            }
        }
        init(new Dimension(500,500), selectTemplates);
    }

    private void init(Dimension size, boolean selectTemplates){
        this.setSize(size);
        ScreenUtil.centerComponentOnScreen(this, this.getOwner());
        this.setResizable(true);
        this.addWindowListener(getCancelChangesAction());
        this.setContentPane(getJPanel());
	/* Enter KeyStroke */
        KeyStroke enter = KeyStroke.getKeyStroke( KeyEvent.VK_ENTER,0,true);
        getJPanel().registerKeyboardAction(getAcceptChangesAction(), enter, JComponent.WHEN_IN_FOCUSED_WINDOW);
        KeyStroke esc = KeyStroke.getKeyStroke( KeyEvent.VK_ESCAPE,0,true);
        getJPanel().registerKeyboardAction(getCancelChangesAction(), esc, JComponent.WHEN_IN_FOCUSED_WINDOW);
        if (selectTemplates){
            getArchetypeTemplateTabbedPane().setSelectedIndex(1);
        }else{
            getArchetypeTemplateTabbedPane().setSelectedIndex(0);
        }
    }

    private JPanel getJPanel() {
        if (jPanel == null) {
            jPanel = new JPanel(new BorderLayout());
            JPanel aux = new JPanel(new FlowLayout(FlowLayout.LEFT));
            aux.add(new JLabel(OpenEHRLanguageManager.getMessage("Domain") + ":"));
            aux.add(getComboBox());
            jPanel.add(aux, BorderLayout.NORTH);
            jPanel.add(getArchetypeTemplateTabbedPane(), BorderLayout.CENTER);
            JPanel panelAux = new JPanel(new BorderLayout());
            jPanel.add(panelAux, BorderLayout.SOUTH);
            panelAux.add(getBottonPanel(), BorderLayout.SOUTH);
        }
        return jPanel;
    }

    private JComboBox getComboBox(){
        if (domainComboBox==null){
            domainComboBox = new JComboBox();
            domainComboBox.addItem(ANY_DOMAIN);
            domainComboBox.addItem(Domains.EHR_ID);
            domainComboBox.addItem(Domains.CDS_ID);
            domainComboBox.setRenderer(new DomainComboBoxRenderer());
            domainComboBox.setSelectedItem(Domains.EHR_ID);
        }
        return domainComboBox;
    }

    private class DomainComboBoxRenderer extends JLabel implements ListCellRenderer{

        private static final long serialVersionUID = 1L;

        @Override
        public Component getListCellRendererComponent(JList list, Object value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            }else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            if (value instanceof String){
                String idDomain = (String)value;
                if (idDomain.equals(ANY_DOMAIN)){
                    idDomain = null;
                }
                setText(DomainsUI.getName(idDomain));
                setToolTipText(DomainsUI.getDescription(idDomain));
                setIcon(DomainsUI.getIcon(idDomain));
            }else{
                setText(null);
                setToolTipText(null);
                setIcon(null);
            }
            return this;
        }
    }

    private JTabbedPane getArchetypeTemplateTabbedPane() {
        if (tabbedPane == null){
            tabbedPane = new JTabbedPane();
            refreshArchetypeSelectionPanel();
            refreshTemplateSelectionPanel();
        }
        return tabbedPane;
    }

    private JPanel getArchetypeSelectionPanel() {
        if(archetypeSelectionPanel==null){
            archetypeSelectionPanel = new SelectionPanel(getArchetypeNode());
            archetypeSelectionPanel.getJTree().expand(getArchetypeNode());
            archetypeSelectionPanel.getJTree().addExtraMouseListener(new DoubleClickMouseListener());
            JButton addArchetypeButton = new JButton(OpenEHRLanguageManager.getMessage("Import"));
            addArchetypeButton.setIcon(OpenEHRImageUtil.FOLDER_ICON);
            addArchetypeButton.addActionListener(new ImportArchetypeActionListener(this));
            archetypeSelectionPanel.getFilterPanel().add(addArchetypeButton);
        }
        return archetypeSelectionPanel;
    }

    private class ImportArchetypeActionListener implements ActionListener {
        private DialogArchetypeChooser _dialog = null;
        public ImportArchetypeActionListener(DialogArchetypeChooser dialog){
            _dialog = dialog;
        }

        public void actionPerformed(ActionEvent e) {
            try {
                ImportUtils.showImportArchetypeDialogAndAddToRepo(_dialog, null);
            } catch (InternalErrorException e1) {
                ExceptionHandler.handle(e1);
            } catch (InstanceNotFoundException e1) {
                ExceptionHandler.handle(e1);
            }
            refreshArchetypeSelectionPanel();
        }
    }



    public void refreshArchetypeSelectionPanel() {
        archetypeSelectionPanel = null;
        archetypeNode = null;
        getArchetypeSelectionPanel();
        if (getArchetypeTemplateTabbedPane().getTabCount()>0){
            getArchetypeTemplateTabbedPane().removeTabAt(0);
        }
        getArchetypeTemplateTabbedPane().insertTab(OpenEHRLanguageManager.getMessage("Archetypes"), Archetypes.ICON, getArchetypeSelectionPanel(), OpenEHRLanguageManager.getMessage("Archetypes"), 0);
        getArchetypeTemplateTabbedPane().setSelectedIndex(0);
    }

    private JPanel getTemplateSelectionPanel() {
        if(templateSelectionPanel==null){
            templateSelectionPanel = new SelectionPanel(getTemplateNode());
            templateSelectionPanel.getJTree().expand(getTemplateNode());
            templateSelectionPanel.getJTree().addExtraMouseListener(new DoubleClickMouseListener());
            JButton addTemplateButton = new JButton(OpenEHRLanguageManager.getMessage("Import"));
            addTemplateButton.setIcon(OpenEHRImageUtil.FOLDER_ICON);
            addTemplateButton.addActionListener(new ImportTemplateActionListener(this));
            templateSelectionPanel.getFilterPanel().add(addTemplateButton);
        }
        return templateSelectionPanel;
    }

    private class ImportTemplateActionListener implements ActionListener {
        private DialogArchetypeChooser _dialog = null;
        public ImportTemplateActionListener(DialogArchetypeChooser dialog){
            _dialog = dialog;
        }

        public void actionPerformed(ActionEvent e) {
            try {
                ImportUtils.showImportTemplateDialog(_dialog, null);
            } catch (InternalErrorException e1) {
                ExceptionHandler.handle(e1);
            } catch (InstanceNotFoundException e1) {
                ExceptionHandler.handle(e1);
            }
            refreshTemplateSelectionPanel();
        }
    }

    public void refreshTemplateSelectionPanel() {
        templateSelectionPanel = null;
        templateNode = null;
        getTemplateSelectionPanel();
        if (getArchetypeTemplateTabbedPane().getTabCount()>1){
            getArchetypeTemplateTabbedPane().removeTabAt(1);
        }
        getArchetypeTemplateTabbedPane().insertTab(
                OpenEHRLanguageManager.getMessage("Templates"),
                Templates.ICON, getTemplateSelectionPanel(),
                OpenEHRLanguageManager.getMessage("Templates"), 1);
        getArchetypeTemplateTabbedPane().setSelectedIndex(1);
    }

    class DoubleClickMouseListener extends MouseAdapter{
        public void mouseClicked(MouseEvent e) {
            if(e.getClickCount()>1){
                CMElement cmElement = getSelectedCMElement();
                if (cmElement!=null){
                    accept();
                }
            }
        }
    }

    protected final void accept() {
        _answer  = true;
        setVisible(false);
    }

    protected final void exit() {
        _answer = false;
        setVisible(false);
    }

    private JPanel getBottonPanel() {
        if (bottonPanel==null){
            bottonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            bottonPanel.add(getAcceptButton());
            bottonPanel.add(getCancelButton());
        }
        return bottonPanel;
    }
    private JButton getAcceptButton() {
        if (acceptButton == null) {
            acceptButton = new JButton();
            acceptButton.setText(OpenEHRLanguageManager.getMessage("Accept"));
            acceptButton.setIcon(OpenEHRImageUtil.ACCEPT_ICON);
            acceptButton.setEnabled(true);
            acceptButton.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            acceptButton.addActionListener(getAcceptChangesAction());
        }
        return acceptButton;
    }

    /**
     * This method initializes cancelButton	
     *
     * @return javax.swing.JButton
     */
    private JButton getCancelButton() {
        if (cancelButton == null) {
            cancelButton = new JButton();
            cancelButton.setText(OpenEHRLanguageManager.getMessage("Cancel"));
            cancelButton.setIcon(OpenEHRImageUtil.CANCEL_ICON);
            cancelButton.setEnabled(true);
            cancelButton.addActionListener(getCancelChangesAction());
        }
        return cancelButton;
    }

    private AcceptChangesAction getAcceptChangesAction() {
        if (acceptChangesAction == null){
            acceptChangesAction = new AcceptChangesAction();
        }
        return acceptChangesAction;
    }

    public class AcceptChangesAction extends AbstractAction {
        private static final long serialVersionUID = -8058749276509227718L;

        public void actionPerformed(ActionEvent e) {
            accept();
        }
    }

    private CancelChangesAction getCancelChangesAction() {
        if (cancelChangesAction == null){
            cancelChangesAction = new CancelChangesAction();
        }
        return cancelChangesAction;
    }

    private class CancelChangesAction extends WindowAdapter implements ActionListener{

        public void windowOpened(WindowEvent e){
        }

        public void actionPerformed(ActionEvent e) {
            exit();
        }

        public void windowClosing(WindowEvent we) {
            exit();
        }
    }

    public SelectableNode<String> getArchetypeNode() {
        if(archetypeNode==null){
            archetypeNode = new SelectableNodeBuilder<String>()
                    .setName(OpenEHRLanguageManager.getMessage("Archetypes"))
                    .setIcon(Archetypes.ICON)
                    .createSelectableNode();
            try {
                Collection<String> archetypeIds = ArchetypeManager.getInstance().getArchetypes().getAllIds();
                insertArchetypeNodes(archetypeNode, archetypeIds, OpenEHRConst.OBSERVATION);
                insertArchetypeNodes(archetypeNode, archetypeIds, OpenEHRConst.ACTION);
                insertArchetypeNodes(archetypeNode, archetypeIds, OpenEHRConst.EVALUATION);
                insertArchetypeNodes(archetypeNode, archetypeIds, OpenEHRConst.INSTRUCTION);
                insertArchetypeNodes(archetypeNode, archetypeIds, OpenEHRConst.ENTRY);
            } catch (InternalErrorException e) {
                ExceptionHandler.handle(e);
            }
        }
        return archetypeNode;
    }

    private static void insertArchetypeNodes(SelectableNode<String> root, Collection<String> archetypeIds, String rmName) {

        SelectableNode<Object> entryRoot = new SelectableNodeBuilder<Object>()
                .setName(OpenEHRConstUI.getName(rmName))
                .setDescription(OpenEHRConstUI.getDescription(rmName))
                .setIcon(OpenEHRConstUI.getIcon(rmName))
                .createSelectableNode();
        for (String archetypeId : archetypeIds) {
            String entryType = Archetypes.getEntryType(archetypeId);
            if (entryType!=null && entryType.equals(rmName)) {
                SelectableNode<String> rnode = new SelectableNodeBuilder<String>()
                        .setName(archetypeId)
                        .setDescription(archetypeId)
                        .setIcon(Archetypes.getIcon(archetypeId))
                        .setObject(archetypeId)
                        .createSelectableNode();
                entryRoot.add(rnode);
            }
        }
        root.add(entryRoot);

    }

    public SelectableNode<String> getTemplateNode() {
        if (templateNode==null){
            templateNode = generateTemplateNode();
        }
        return templateNode;
    }

    public static SelectableNode<String> generateTemplateNode() {
        SelectableNode templateNode = new SelectableNodeBuilder<String>()
                .setName(OpenEHRLanguageManager.getMessage("Templates"))
                .setIcon(Templates.ICON)
                .createSelectableNode();
        try {
            Collection<String> templateIds = ArchetypeManager.getInstance().getTemplates().getAllIds();
            insertTemplateNodes(templateNode, templateIds);
        } catch (InternalErrorException e){
            ExceptionHandler.handle(e);
        }
        return templateNode;
    }

    private static void insertTemplateNodes(SelectableNode<String> rootNode, Collection<String> templateIds) {
        for (String templateId : templateIds) {
            SelectableNode<String> node = new SelectableNodeBuilder<String>()
                    .setName(templateId)
                    .setObject(templateId)
                    .setIcon(Templates.ICON)
                    .createSelectableNode();
            rootNode.add(node);
        }
    }

    public boolean getAnswer(){
        return _answer;
    }


    public CMElement getSelectedCMElement() {
        boolean isArchetypeTabSelected = getArchetypeTemplateTabbedPane().getSelectedIndex() == 0;
        try {
            String id = getSelectedId(isArchetypeTabSelected);
            if (id == null){
                return null;
            } else {
                return getCmElement(id, isArchetypeTabSelected);
            }
        } catch (InternalErrorException e1) {
            ExceptionHandler.handle(e1);
        } catch (InstanceNotFoundException e1) {
            ExceptionHandler.handle(e1);
        }
        return null;
    }

    private CMElement getCmElement(String id, boolean isArchetypeTabSelected) throws InstanceNotFoundException, InternalErrorException {
        if (isArchetypeTabSelected) {
            return ArchetypeManager.getInstance().getArchetypes().getCMElement(id);
        } else {
            return ArchetypeManager.getInstance().getTemplates().getCMElement(id);
        }
    }

    private String getSelectedId(boolean isArchetypeTabSelected) {
        if (isArchetypeTabSelected) {
            return NodeConversor.getSelectedElement(getArchetypeNode());
        } else {
            return NodeConversor.getSelectedElement(getTemplateNode());
        }
    }

    public String getSelectedDomain() {
        String idDomain = (String)getComboBox().getSelectedItem();
        if (idDomain.equals(ANY_DOMAIN)){
            idDomain = null;
        }
        return idDomain;
    }
}
/*
 *  ***** BEGIN LICENSE BLOCK *****
 *  Version: MPL 2.0/GPL 2.0/LGPL 2.1
 *
 *  The contents of this file are subject to the Mozilla Public License Version
 *  2.0 (the 'License'); you may not use this file except in compliance with
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