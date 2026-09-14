;;; Sierra Script 1.0 - (do not remove this comment)
;
; SCI Template Game
; By Brian Provinciano
; ******************************************************************************
; menubar.sc
; Contains the customized Menubar class. This is the script you modify if you 
; want to customize the menu.
(script# MENUBAR_SCRIPT)
(include sci.sh)
(include game.sh)
(use main)
(use controls)
(use gauge)
(use sound)
(use user)

(public
	ToggleSound 0
)




(class TheMenuBar of MenuBar
	(properties
		state 0
	)
	
	(method (init)
		(AddMenu { \01_} {About Template`^a:Help`#1})
		(AddMenu
			{ File_}
			{Restart Game`#9:Save Game`#5:Restore Game`#7:--! :Quit`^q}
		)
		(AddMenu
			{ Action_}
			{Pause Game`^p:Inventory`^I:Retype`#3:--! :Colors`^c}
		)
		(AddMenu
			{ Speed_}
			{Change...`^s:--!:Faster`+:Normal`=:Slower`-}
		)
		(if (DoSound sndSET_SOUND)
			(AddMenu { Sound_} {Volume...`^v:Turn Off`#2=1})
		else
			(AddMenu { Sound_} {Volume...`^v:Turn On`#2=1})
		)
		(if (< (Graph grGET_COLOURS) 9)
			(SetMenu MENU_COLOURS 32 0)
		else
			(SetMenu MENU_COLOURS smMENU_SAID '/color')
		)
		(SetMenu MENU_SAVE smMENU_SAID 'save[/game]')
		(SetMenu MENU_RESTORE smMENU_SAID 'restore[/game]')
		(SetMenu MENU_RESTART smMENU_SAID 'restart[/game]')
		(SetMenu MENU_QUIT smMENU_SAID 'done[/game]')
		(SetMenu MENU_PAUSE smMENU_SAID 'delay[/game]')
		(SetMenu MENU_INVENTORY smMENU_SAID 'all')
	)
	
	(method (handleEvent pEvent &tmp menuItem hGauge newSpeed newVolume wndCol wndBack hPause)
		(= menuItem (super handleEvent: pEvent))
		(switch menuItem
			(MENU_ABOUT
				(Print
					{_______Template Game\n By Brian Provinciano}
					#title
					{About}
				)
			)
			(MENU_HELP
				(Print
					{<Put your how to play stuff here>}
					#title
					{How To Play}
				)
			)
			(MENU_RESTART
				(if
					(Print
						{Are you serious? You really want to start all the way back at the beginning again?}
						#title
						{Restart}
						#font
						gDefaultFont
						#button
						{Restart}
						1
						#button
						{ Oops_}
						0
					)
					(gGame restart:)
				)
			)
			(MENU_RESTORE (gGame restore:))
			(MENU_SAVE (gGame save:))
			(MENU_QUIT
				(if
					(Print
						{Do you really want to quit?}
						#title
						{Quit}
						#font
						gDefaultFont
						#button
						{ Quit_}
						1
						#button
						{ Oops_}
						0
					)
					(= gQuitGame TRUE)
				)
			)
			(MENU_PAUSE
				(= hPause (Sound pause:))
				(Print {Game Paused})
				(Sound pause: hPause)
			)
			(MENU_INVENTORY
				(if (PrintCantDoThat $0400) (gInv showSelf: gEgo))
			)
			(MENU_RETYPE
				(pEvent
					claimed: FALSE
					type: evKEYBOARD
					message: (User echo?)
				)
			)
			(MENU_COLOURS
				(= wndCol 16)
				(while (and (u> wndCol 15) (!= wndCol -1))
					(= wndCol (GetNumber {New Text Color: (0-15)}))
				)
				(if (!= wndCol -1)
					(= wndBack 16)
					(while
						(or
							(and (!= wndBack -1) (u> wndBack 15))
							(== wndCol wndBack)
						)
						(= wndBack (GetNumber {New Background Color: (0-15)}))
					)
					(if (!= wndBack -1)
						(= gWndColor wndCol)
						(= gWndBack wndBack)
						(gTheWindow color: gWndColor back: gWndBack)
					)
				)
			)
			(MENU_CHANGESPEED
				(= hGauge (Gauge new:))
				(= newSpeed
					(hGauge
						text: {Game Speed}
						description:
							{Use the mouse or the left and right arrow keys to select the game speed.}
						higher: {Faster}
						lower: {Slower}
						normal: NORMAL_SPEED
						doit: (- 15 gSpeed)
					)
				)
				(gGame setSpeed: (- 15 newSpeed))
				(DisposeScript GAUGE_SCRIPT)
			)
			(MENU_FASTERSPEED
				(if gSpeed (gGame setSpeed: (-- gSpeed)))
			)
			(MENU_NORMALSPEED
				(if gSpeed (gGame setSpeed: 12))
			)
			(MENU_SLOWERSPEED
				(if (< gSpeed 15) (gGame setSpeed: (++ gSpeed)))
			)
			(MENU_VOLUME
				(= hGauge (Gauge new:))
				(= newVolume
					(hGauge
						text: {Sound Volume}
						description:
							{Use the mouse or the left and right arrow keys to adjust the volume.}
						higher: {Louder}
						lower: {Softer}
						normal: 15
						doit: (DoSound sndVOLUME newVolume)
					)
				)
				(DoSound sndVOLUME newVolume)
				(DisposeScript GAUGE_SCRIPT)
			)
			(MENU_TOGGLESOUND (ToggleSound))
		)
	)
)


(procedure (ToggleSound &tmp SOUND_OFF)
	(= SOUND_OFF (DoSound sndSET_SOUND))
	(= SOUND_OFF (DoSound sndSET_SOUND (not SOUND_OFF)))
	(if SOUND_OFF
		(SetMenu MENU_TOGGLESOUND smMENU_TEXT {Turn On})
	else
		(SetMenu MENU_TOGGLESOUND smMENU_TEXT {Turn Off})
	)
)
