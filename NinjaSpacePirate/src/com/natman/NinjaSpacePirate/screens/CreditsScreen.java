package com.natman.NinjaSpacePirate.screens;

import com.lostcode.javalib.Game;
import com.natman.NinjaSpacePirate.screens.MenuButton.ButtonCallback;

public class CreditsScreen extends MenuScreen {

	public CreditsScreen(Game game) {
		super(game, "Credits");
		
		MenuButton line1 = new MenuButton(font, "Game by");
		MenuButton line2 = new MenuButton(font, "Nathaniel Nelson");
		MenuButton line3 = new MenuButton(font, "");
		MenuButton line4 = new MenuButton(font, "With art by");
		MenuButton line5 = new MenuButton(font, "Oryx Design Lab");
		MenuButton line6 = new MenuButton(font, "");
		MenuButton exitButton = new MenuButton(font, "Back");
		
		exitButton.onTrigger = new ButtonCallback() {

			@Override
			public void invoke(Game game) {
				exit();
			}
			
		};
		
		buttons.add(line1);
		buttons.add(line2);
		buttons.add(line3);
		buttons.add(line4);
		buttons.add(line5);
		buttons.add(line6);
		buttons.add(exitButton);
		
		buttonScale = 0.95f;
	}

}
