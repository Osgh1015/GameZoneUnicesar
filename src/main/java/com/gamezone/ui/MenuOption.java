package com.gamezone.ui;

/**
 * One option of the main menu: the submenu of a module. The main menu
 * prints the title of each option and runs the one the user chooses, so a
 * new module is added by implementing this interface and passing the
 * option to {@link ConsoleMenu}, without changing it.
 */
public interface MenuOption {

    /**
     * Returns the text shown for this option in the main menu.
     *
     * @return the title of the option, in Spanish
     */
    String getTitle();

    /**
     * Shows the submenu of this option and executes what the user chooses.
     */
    void show();
}
