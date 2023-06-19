package ru.itis.dtoidea;

public class ShowPopup {//implements ProjectComponent {

//    private Project project;
//    public ShowPopup(Project project) {
//        this.project = project;
//    }
//
//    Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
//    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
//
//
//    @Override
//    public void projectOpened() {
//        IdeFocusManager.getInstance(project).doWhenFocusSettlesDown(() -> {
//            KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(keyEvent -> {
//                if (keyEvent.getKeyCode() == KeyEvent.VK_1 && keyEvent.isControlDown()) {
//                    showPopup();
//                }
//                return false;
//            });
//        });
//    }
//
//    @Override
//    public void projectClosed() {
//        executorService.shutdown();
//    }
//
//    private void showPopup() {
//        SwingUtilities.invokeLater(() -> JBPopupFactory.getInstance()
//                .createConfirmation("В некоторых моделях отсутсвует аннотация @DtoMapping. Добавить ее?", null, 0).showInBestPositionFor(editor));
//
//    }

}
