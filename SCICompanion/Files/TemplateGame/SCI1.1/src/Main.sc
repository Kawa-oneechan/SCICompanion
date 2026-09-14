;;; Sierra Script 1.0 - (do not remove this comment)
;	
;	 This is the main game script. It contains the main game class, all the global variables, and
;	 a number of useful procedures.
;	
;	 In addition to the above, it contains the icon instances for the icons
;	 in the control bar. It also contains the crucial default Messager
;	 and its findTalker method (used for mapping talker numbers to a Talker or Narrator instance).
(script# 0)
(include sci.sh)
(include game.sh)
(include 0.shm)
(use ColorInit)
(use Smopper)
(use GameEgo)
(use ScrollableInventory)
(use ScrollInsetWindow)
(use DColorButton)
(use SpeakWindow)
(use Print)
(use DialogControls)
(use Messager)
(use Talker)
(use PseudoMouse)
(use Scaler)
(use BorderWindow)
(use IconItem)
(use RandCycle)
(use PolyPath)
(use Polygon)
(use StopWalk)
(use Timer)
(use Grooper)
(use Sound)
(use Game)
(use User)
(use System)
(use File)

(public
	SQ5 0
	Btest 1
	Bset 2
	Bclear 3
	RestorePreviousHandsOn 4
	IsObjectOnControl 5
	SetUpEgo 6
	AddToScore 7
	AimToward 8
	Die 9
	ScoreFlag 10
	HideStatus 11
	DebugPrint 12
)
(define STARTING_ROOM 100)

(local
	gEgo                ; The object for the player's ego.
	gGame               ; The game object.
	gRoom               ; The current room object.
	global3     ; Unused
	gQuitGame =  FALSE
	gCast
	gRegions            ; The current regions.
	gTimers             ; The current timers.
	gSounds             ; The current sounds.
	gInv                ; The inventory.
	gAddToPics
	gRoomNumber         ; The current room number
	gPreviousRoomNumber
	gNewRoomNumber
	gDebugOnNextRoom
	gScore              ; The player's current score.
	gMaxScore           ; The maximum score.
	gTextCode
	gCuees
	gCursorNumber
	gNormalCursor =  999
	gWaitCursor =  997
	gFont =  1          ; Main font number.
	gSmallFont =  4     ; Small font number.
	gPEvent             ; The current event.
	gDialog             ; The current Print dialog.
	gBigFont =  1       ; Big font number.
	gVersion
	gSaveDir
	gPicAngle
	gFeatures
	gUseSortedFeatures
	gPicNumber =  -1
	gDoMotionCue
	gWindow
	global39    ; Unused
	global40    ; Unused
	gOldPort
	[gDebugFilename 21]     ; debug filename
	gGameControls           ; The main GameControls class.
	gFeatureInit            ; Code that initializes all features.
	gDoVerbCode
	gApproachCode
	gEgoUseObstacles =  1   ; Default Motion type for ego (0: MoveTo, 1: PolyPath, ...)
	gIconBar
	gPEventX                ; Current event's x value.
	gPEventY                ; Current event's y value.
	gOldKH
	gOldMH
	gOldDH
	gPseudoMouse
	gTheDoits
	gEatTheMice =  60       ; Number of ticks before we mouse
	gUser
	gSyncBias               ; Something to do with lip-sync.
	gTheSync
	global83                ; Something to do with audio narration.
	gFastCast
	gInputFont
	gTickOffset             ; Something to do with time (ticks per frame?)
	gGameTime
	gNarrator               ; Default Narrator.
	gMessageType =  $0001   ; Talker flags: 0x1 (text) and 0x2 (audio).
	gMessager
	gPrints
	gWalkHandler
	gTextReadSpeed =  2
	gAltPolyList
	gColorDepth
	gPolyphony
	gStopGroop
	global107
	gCurrentIcon
	gGUserCanControl
	gGUserCanInput
	gCheckedIcons
	gState
	gNewSpeakWindow
	gWindow2
	gMusic1
	gDongle =  1234             ; This variable CAN'T MOVE
	gMusic2
	gCurrentTalkerNumber
	gGEgoMoveSpeed
	gColorWindowForeground
	gColorWindowBackground
	gLowlightColor
	gDefaultEgoView =  0        ; The default view resource for the ego
	gRegister
	[gFlags 14]                 ; Start of bit set. Room for 14 x 16 = 224 flags.
	gEdgeDistance =  10         ; Margin around screen to make it easier to walk the ego to the edge.
	gDebugOut
)
;	
;	 Tests a boolean game flag.
;	
;	 :param number flag: The number of the flag to test.
;	 :returns: TRUE if the flag is set, otherwise FALSE.
;	
;	 Example usage::
;	
;	 	(if (not (Btest FLAG_OpenedSewer))
;	 		(Prints {You can't enter, the sewer is closed.})
;	 	)
(procedure (Btest flag)
	(return (& [gFlags (/ flag 16)] (>> $8000 (mod flag 16))))
)

;	
;	 Sets a boolean game flag.
;	
;	 :param number flag: The number of the flag to set.
;	 :returns: The previous value of the flag (TRUE or FALSE).
;	
;	 Example usage::
;	
;	 	(V_DO
;	 		(Bset FLAG_OpenedSewer)
;	 		(sewer setCel: 3)
;	 	)
(procedure (Bset flag &tmp temp0)
	(= temp0 (Btest flag))
	(= [gFlags (/ flag 16)]
		(| [gFlags (/ flag 16)] (>> $8000 (mod flag 16)))
	)
	(return temp0)
)

;	
;	 Clears a boolean game flag.
;	
;	 :param number flag: The number of the flag to clear.
;	 :returns: The previous value of the flag (TRUE or FALSE).
(procedure (Bclear flag &tmp temp0)
	(= temp0 (Btest flag))
	(= [gFlags (/ flag 16)]
		(& [gFlags (/ flag 16)] (~ (>> $8000 (mod flag 16))))
	)
	(return temp0)
)

(procedure (RestorePreviousHandsOn &tmp temp0)
	(gUser
		canControl: gGUserCanControl
		canInput: gGUserCanInput
	)
	(= temp0 0)
	(while (< temp0 8)
		(if (& gCheckedIcons (>> $8000 temp0))
			(gIconBar disable: temp0)
		)
		(++ temp0)
	)
)

;	
;	 Tests if the origin of an object is on the control color.
;	
;	 :param heapPtr theActor: An :class:`Actor` object.
;	 :param number ctlColor: A control color (such as ctlLIME or ctlWHITE).
;	 :returns: TRUE if the object is on the control color, FALSE otherwise.
(procedure (IsObjectOnControl theActor ctlColor)
	(return
		(if (& (theActor onControl: TRUE) ctlColor)
			(return 1)
		else
			0
		)
	)
)

;
; .. function:: SetUpEgo([theView theLoop])
;
; 	Used to set up the ego, generally in a room's init() method.
; 	
; 	:param number theView: The view to use, or -1 to use gDefaultEgoView.
; 	:param number theLoop: The loop to use.
(procedure (SetUpEgo theView theLoop)
	(if (and (> argc 0) (!= theView -1))
		(gEgo view: theView)
		(if (and (> argc 1) (!= theLoop -1))
			(gEgo loop: theLoop)
		)
	else
		(gEgo view: gDefaultEgoView)
		(if (and (> argc 1) (!= theLoop -1))
			(gEgo loop: theLoop)
		)
	)
	(if (gEgo looper?) ((gEgo looper?) dispose:))
	(gEgo
		setStep: 5 2
		illegalBits: 0
		ignoreActors: 0
		setSpeed: gGEgoMoveSpeed
		; signal(| (send gEgo:signal) $1000)
		heading:
			(switch (gEgo loop?)
				(0 90)
				(1 270)
				(2 180)
				(3 0)
				(4 135)
				(5 225)
				(6 45)
				(7 315)
			)
	)
	(gEgo
		setLoop: -1
		setLoop: stopGroop
		setPri: -1
		setMotion: NULL
		state: (| (gEgo state?) $0002)
	)
)

;
; .. function:: AimToward(theObj otherObj [cueObj])
;
; .. function:: AimToward(theObj otherObj faceEachOther [cueObj])
;
; .. function:: AimToward(theObj x y [cueObj])
;
; 	Changes the heading of theObj so it faces another object or position.
; 	Optionally causes the other object to face the first object.
;
; 	:param heapPtr theObj: The object that is being aimed.
; 	:param heapPtr otherObj: The target object.
; 	:param boolean faceEachOther: If TRUE, the otherObj will also be made to face theObj.
; 	:param number x: The target x.
; 	:param number y: The target x.
; 	:param heapPtr cueObj: Optional object to be cue()'d.
(procedure (AimToward theObj param2 param3 param4 &tmp theAngle theX theY cueObject someFlag)
	(= cueObject 0)
	(= someFlag 0)
	(if (IsObject param2)
		(= theX (param2 x?))
		(= theY (param2 y?))
		(if (> argc 2)
			(if (IsObject param3)
				(= cueObject param3)
			else
				(= someFlag param3)
			)
			(if (== argc 4) (= cueObject param4))
		)
	else
		(= theX param2)
		(= theY param3)
		(if (== argc 4) (= cueObject param4))
	)
	(if someFlag (AimToward param2 theObj))
	(= theAngle
		(GetAngle (theObj x?) (theObj y?) theX theY)
	)
	(theObj
		setHeading: theAngle (if (IsObject cueObject) cueObject else 0)
	)
)

;
; .. function:: Die([theDeathReason])
;
; 	Causes the ego to die. The global variable gDeathReason will be set to the death reason.
; 	
; 	:param number theDeathReason: An arbitrary numerical value to be interpreted by the DeathRoom.sc script.
(procedure (Die theDeathReason)
	; TODO
	(gGame setCursor: gNormalCursor true)
	(repeat
		(switch
			(Print
				font: gFont
				addText: "lol. lmao even." 0 0
				addButton: 1 {Restore} 0 32
				addButton: 2 {Restart} 54 32
				addButton: 3 {__Quit__} 108 32
				init:
			)
			(1
				(gGame restore:)
			)
			(2
				(gGame restart:)
			)
			(3
				(= gQuitGame true)
				(break)
			)
		)
	)
)

;	
;	 Adds an amount to the player's current score.
;	
;	 :param number amount: The amount to add to the score (can be negative).
(procedure (AddToScore amount)
	(= gScore (+ gScore amount))
	(statusLineCode doit:)
	(rm0Sound
		priority: 15
		number: 1000
		loop: 1
		flags: 1
		play:
	)
)

;	
;	 Adds an amount to the player's current score. A flag (one used with
;	 :func:`BSet`, :func:`BClear` or :func:`BTest`) must be provided. This
;	 ensures that a score is only added once.
;	
;	 :param number flag: A flag indicating what this score is for.
;	 :param number amount: The amount to add to the score.
(procedure (ScoreFlag flag amount)
	(if (not (Btest flag))
		(AddToScore amount)
		(Bset flag)
	)
)

;	
;	 Hides the status bar.
(procedure (HideStatus &tmp temp0)
	(= temp0 (GetPort))
	(SetPort -1)
	(Graph grFILL_BOX 0 0 10 320 VISUAL 0 -1 -1)
	(Graph grUPDATE_BOX 0 0 10 320 VISUAL)
	(SetPort temp0)
)

;
; .. function:: DebugPrint(theText [params ...])
;
; 	Prints a debug message that can be displayed in SCI Companion. The text may contain the following formatting
; 	characters:
; 	
; 	%d
; 		Formats a number in decimal.
; 		
; 	%x
; 		Formats a number in hexadecimal.
;
; 	%s
; 		Formats a string.
;
; 	:param string theText: A string of text containing formatting characters.
; 	
; 	Example usage::
; 	
; 		(DebugPrint {You are in room %d} gNewRoomNumber)
(procedure (DebugPrint)
	(if gDebugOut (gDebugOut debugPrint: &rest))
)

(instance rm0Sound of Sound
	(properties
		priority 15
	)
)

(instance music1 of Sound
	(properties
		flags $0001
	)
)

(instance music2 of Sound
	(properties
		flags $0001
	)
)

(instance stopGroop of GradualLooper
	(properties)
)

(instance ego of GameEgo
	(properties)
)

(instance statusLineCode of Code
	(properties)
	
	(method (doit &tmp [str 64] [fmt 64])
		(Message msgGET 0 N_TITLEBAR 0 0 1 @fmt)
		(Format @str @fmt gScore gMaxScore)
		(DrawStatus @str 4 2)
		; Remove the two numbers at the end for a black on white bar.
	)
)

(instance templateIconBar of IconBar
	(properties)
	
	(method (show)
		(if (IsObject curInvIcon) (curInvIcon loop: 2))
		(super show:)
		(if (IsObject curInvIcon) (curInvIcon loop: 1))
	)
	
	(method (hide)
		(super hide: &rest)
		(gGame setCursor: gCursorNumber 1)
	)
	
	(method (noClickHelp &tmp temp0 temp1 temp2 temp3 winEraseOnly)
		(= temp2 0)
		(= temp1 temp2)
		(= temp3 (GetPort))
		(= winEraseOnly (gWindow eraseOnly?))
		(gWindow eraseOnly: 1)
		(while
		(not ((= temp0 ((gUser curEvent?) new:)) type?))
			(if (not (self isMemberOf: IconBar)) (temp0 localize:))
			(= temp2 (self firstTrue: #onMe temp0))
			(cond 
				(temp2
					(if
						(and
							(!= (= temp2 (self firstTrue: #onMe temp0)) temp1)
							(temp2 helpVerb?)
						)
						(= temp1 temp2)
						(if gDialog (gDialog dispose:))
						(Print
							font: gFont
							width: 250
							addText: (temp2 noun?) (temp2 helpVerb?) 0 1 0 0 (temp2 modNum?)
							modeless: 1
							init:
						)
						(Animate (gCast elements?) 0)
						(SetPort temp3)
					)
				)
				(gDialog (gDialog dispose:) (Animate (gCast elements?) 0))
				(else (= temp1 0))
			)
			(temp0 dispose:)
		)
		(gWindow eraseOnly: winEraseOnly)
		(gGame setCursor: 999 1)
		(if gDialog
			(gDialog dispose:)
			(Animate (gCast elements?) 0)
		)
		(SetPort temp3)
		(if (not (helpIconItem onMe: temp0))
			(self dispatchEvent: temp0)
		)
	)
)

; In order for this game to run in ScummVM, the game name needs to
; be a known one (e.g. SQ5)
;	
;	 The main game class. This subclasses :class:`Game` and adds game-specific functionality.
(class SQ5 of Game
	(properties
		script 0
		printLang 1
		_detailLevel 3
		panelObj 0
		panelSelector 0
		handsOffCode 0
		handsOnCode 0
	)
	
	;	
	;	 Modify this to set up any initial state the game needs. Among the things set here are:
	;	
	;	 - The maximum score.
	;	 - Text colors and fonts used in messages.
	;	 - The action icons.
	;	 - The default game cursor.
	;	
	;	
	(method (init &tmp [temp0 7] temp7)
		((ScriptID INVENTORY_SCRIPT 0) init:)
		(super init:)
		(= gEgo ego)
		(User alterEgo: gEgo canControl: 0 canInput: 0)
		(= gMessageType $0001)
		(= gUseSortedFeatures TRUE)
		(= gPolyphony (DoSound sndGET_POLYPHONY))
		(= gMaxScore 5000)
		(= gGEgoMoveSpeed 6)
		(= gEatTheMice 30)
		(= gTextReadSpeed 2)
		(= gColorDepth (Graph grGET_COLOURS))
		(= gStopGroop stopGroop)
		(= gPseudoMouse PseudoMouse)
		(gEgo setLoop: gStopGroop)
		; The position of these font resource numbers correspond to font codes used in messages:
		(TextFonts 1 2 4 0)
		; These correspond to color codes used in messages (values into global palette):
		(TextColors 0 15 26 31 34 52 63)
		(= gVersion {x.yyy.zzz})
		(= temp7 (FileIO fiOPEN {version} fOPENFAIL))
		(FileIO fiREAD_STRING gVersion 11 temp7)
		(FileIO fiCLOSE temp7)
		(ColorInit)
		(DisposeScript COLORINIT_SCRIPT)
		(= gNarrator templateNarrator)
		(= gWindow mainWindow)
		(= gWindow2 mainWindow)
		(= gMessager testMessager)
		(= gNewSpeakWindow (SpeakWindow new:))
		(gWindow
			color: gColorWindowForeground
			back: gColorWindowBackground
		)
		(gGame
			setCursor: gCursorNumber TRUE 304 172
			detailLevel: 3
		)
		(= gMusic1 music1)
		(gMusic1 owner: self flags: 1 init:)
		; number(1)
		(= gMusic2 music2)
		(gMusic2 owner: self flags: 1 init:)
		; number(1)
		(= gIconBar templateIconBar)
		(gIconBar
			; These correspond to ICONINDEX_*** in game.sh
			add: icon0 icon1 icon2 icon3 icon4 icon6 icon7 icon8 icon9
			eachElementDo: #init
			eachElementDo: #highlightColor 0
			eachElementDo: #lowlightColor 5
			curIcon: icon0
			useIconItem: icon6
			helpIconItem: icon9
			walkIconItem: icon0
			disable: ICONINDEX_CURITEM
			state: 3072
			disable:
		)
		(= gNormalCursor 999)
		(= gWaitCursor 996)
		(= gDoVerbCode lb2DoVerbCode)
		(= gFeatureInit lb2FtrInit)
		(= gApproachCode lb2ApproachCode)
	)
	
	(method (doit)
		(if (GameIsRestarting)
			(if (IsOneOf gRoomNumber TITLEROOM_SCRIPT)
				(HideStatus)
			else
				(statusLineCode doit:)
			)
			(= gColorDepth (Graph grGET_COLOURS))
		)
		(super doit: &rest)
	)
	
	(method (play &tmp deleteMe debugRoom theStartRoom)
		(= gGame self)
		(= gSaveDir (GetSaveDir))
		(if (not (GameIsRestarting)) (GetCWD gSaveDir))
		(self setCursor: gWaitCursor 1 init:)
		(= theStartRoom STARTING_ROOM)
		(if (not (GameIsRestarting))
			(if (FileIO fiEXISTS {sdebug.txt})
				(= gDebugOut (ScriptID DEBUGOUT_SCRIPT 0))
				(DebugPrint {Debugger enabled})
				(= debugRoom (gDebugOut init: {sdebug.txt}))
				(if (!= debugRoom -1)
					(= theStartRoom debugRoom)
					(gGame handsOn:)
					(DebugPrint {Starting in room %d} theStartRoom)
				)
			)
		)
		(self newRoom: theStartRoom)
		(while (not gQuitGame)
			(self doit:)
		)
	)
	
	(method (startRoom param1 &tmp [temp0 4])
		(if (IsOneOf param1 TITLEROOM_SCRIPT)
			(HideStatus)
		else
			(statusLineCode doit:)
		)
		(if gPseudoMouse (gPseudoMouse stop:))
		((ScriptID DISPOSECODE_SCRIPT) doit: param1)
		(super startRoom: param1)
	)
	
	(method (restart &tmp temp0 temp1)
		(= temp1 ((gIconBar curIcon?) cursor?))
		(gGame setCursor: 999)
		(= temp0
			(Print
				font: gFont
				width: 75
				window: gWindow
				mode: 1
				addText: N_RESTART V_LOOK 0 1 0 0 0
				addColorButton: 1 N_RESTART V_LOOK 0 2 0 40 0
				addColorButton: 0 N_RESTART V_LOOK 0 3 0 50 0
				init:
			)
		)
		(if temp0
			(super restart: &rest)
		else
			(gGame setCursor: temp1)
		)
	)
	
	(method (restore &tmp [temp0 2])
		(super restore: &rest)
		(gGame setCursor: ((gIconBar curIcon?) cursor?))
	)
	
	(method (save)
		(super save: &rest)
		(gGame setCursor: ((gIconBar curIcon?) cursor?))
	)
	
	;	
	;	 Modify this method to change any global keyboard bindings.
	;	
	(method (handleEvent pEvent &tmp theGCursorNumber)
		(super handleEvent: pEvent)
		(if (pEvent claimed?) (return 1))
		(return
			(switch (pEvent type?)
				(evKEYBOARD
					(switch (pEvent message?)
						(KEY_TAB
							(if
							(not (& ((gIconBar at: 6) signal?) icDISABLED))
								(if gFastCast (return gFastCast))
								(= theGCursorNumber gCursorNumber)
								(gInv showSelf: gEgo)
								(gGame setCursor: theGCursorNumber 1)
								(pEvent claimed: TRUE)
							)
						)
						(KEY_CONTROL
							(if
							(not (& ((gIconBar at: 7) signal?) icDISABLED))
								(gGame quitGame:)
								(pEvent claimed: TRUE)
							)
						)
						(JOY_RIGHT
							(if
							(not (& ((gIconBar at: 7) signal?) icDISABLED))
								(= theGCursorNumber ((gIconBar curIcon?) cursor?))
								((ScriptID 24 0) doit:)
								(gGameControls dispose:)
								(gGame setCursor: theGCursorNumber 1)
							)
						)
						(KEY_F2
							(cond 
								((gGame masterVolume:) (gGame masterVolume: 0))
								((> gPolyphony 1) (gGame masterVolume: 15))
								(else (gGame masterVolume: 1))
							)
							(pEvent claimed: TRUE)
						)
						(KEY_F5
							(if
							(not (& ((gIconBar at: 7) signal?) icDISABLED))
								(if gFastCast (return gFastCast))
								(= theGCursorNumber gCursorNumber)
								(gGame save:)
								(gGame setCursor: theGCursorNumber 1)
								(pEvent claimed: TRUE)
							)
						)
						(KEY_F7
							(if
							(not (& ((gIconBar at: 7) signal?) icDISABLED))
								(if gFastCast (return gFastCast))
								(= theGCursorNumber gCursorNumber)
								(gGame restore:)
								(gGame setCursor: theGCursorNumber 1)
								(pEvent claimed: TRUE)
							)
						)
						(KEY_EXECUTE
							(if (gUser controls?)
								(= gGEgoMoveSpeed (gEgo moveSpeed?))
								(= gGEgoMoveSpeed (Max 0 (-- gGEgoMoveSpeed)))
								(gEgo setSpeed: gGEgoMoveSpeed)
							)
						)
						(KEY_SUBTRACT
							(if (gUser controls?)
								(= gGEgoMoveSpeed (gEgo moveSpeed?))
								(gEgo setSpeed: (++ gGEgoMoveSpeed))
							)
						)
						(61
							(if (gUser controls?) (gEgo setSpeed: 6))
						)
						(KEY_ALT_v
							(Print
								addText: {Version number:} 0 0
								addText: gVersion 0 14
								init:
							)
						)
						(KEY_ALT_d
							; Script-base debugger
							((ScriptID INGAME_DEBUG_SCRIPT 0) init:)
						)
						(else  (pEvent claimed: FALSE))
					)
				)
			)
		)
	)
	
	(method (setCursor cursorNumber param2 param3 param4 &tmp theGCursorNumber)
		(= theGCursorNumber gCursorNumber)
		(if argc
			(if (IsObject cursorNumber)
				(= gCursorNumber cursorNumber)
				(gCursorNumber init:)
			else
				(= gCursorNumber cursorNumber)
				(SetCursor gCursorNumber 0 0)
			)
		)
		(if (and (> argc 1) (not param2)) (SetCursor 996 0 0))
		(if (> argc 2) (SetCursor param3 param4))
		(return theGCursorNumber)
	)
	
	(method (quitGame &tmp temp0 temp1)
		(= temp1 ((gIconBar curIcon?) cursor?))
		(gGame setCursor: 999)
		(= temp0
			(Print
				font: gFont
				width: 75
				mode: 1
				addText: N_QUITMENU V_LOOK 0 1 0 0 0
				addColorButton: 1 N_QUITMENU V_LOOK 0 2 0 25 0
				addColorButton: 0 N_QUITMENU V_LOOK 0 3 0 35 0
				init:
			)
		)
		(if temp0
			(Print addText: 19 1 0 4 0 0 0 init:)
			(super quitGame: &rest)
		else
			(gGame setCursor: temp1)
		)
	)
	
	;	
	;	 Modify this method to add any default messages for actions.
	;	
	(method (pragmaFail)
		(if (User canControl:)
			(switch ((gUser curEvent?) message?)
				(V_DO
					(gMessager say: 0 V_DO 0 (Random 1 2) 0 0)
				)
				(V_TALK
					(gMessager say: 0 V_TALK 0 (Random 1 2) 0 0)
				)
				(else 
					(if
					(not (IsOneOf ((gUser curEvent?) message?) V_LOOK))
						(gMessager say: 0 V_COMBINE 0 (Random 2 3) 0 0)
					)
				)
			)
		)
	)
	
	;	
	;	 This disables player control (e.g. for cutscenes).
	;	
	(method (handsOff)
		(if (not gCurrentIcon)
			(= gCurrentIcon (gIconBar curIcon?))
		)
		(= gGUserCanControl (gUser canControl:))
		(= gGUserCanInput (gUser canInput:))
		(gUser canControl: 0 canInput: 0)
		(gEgo setMotion: 0)
		(= gCheckedIcons 0)
		(gIconBar eachElementDo: #perform checkIcon)
		(gIconBar curIcon: (gIconBar at: 7))
		(gIconBar disable:)
		(gIconBar
			disable:
				ICONINDEX_WALK
				ICONINDEX_LOOK
				ICONINDEX_DO
				ICONINDEX_TALK
				ICONINDEX_CUSTOM
				ICONINDEX_CURITEM
				ICONINDEX_INVENTORY
				ICONINDEX_SETTINGS
		)
		(gGame setCursor: 996)
	)
	
	;	
	;	 This re-enables player control after having been disabled.
	;	
	(method (handsOn fRestore)
		(gIconBar enable:)
		(gUser canControl: 1 canInput: 1)
		(gIconBar
			enable:
				ICONINDEX_WALK
				ICONINDEX_LOOK
				ICONINDEX_DO
				ICONINDEX_TALK
				; ICONINDEX_CUSTOM // see below
				ICONINDEX_CURITEM
				ICONINDEX_INVENTORY
				ICONINDEX_SETTINGS
		)
		(gIconBar disable: ICONINDEX_CUSTOM)
		; See above
		(if (and argc fRestore) (RestorePreviousHandsOn))
		(if (not (gIconBar curInvIcon?))
			(gIconBar disable: ICONINDEX_CURITEM)
		)
		(if gCurrentIcon
			(gIconBar curIcon: gCurrentIcon)
			(gGame setCursor: (gCurrentIcon cursor?))
			(= gCurrentIcon 0)
			(if
				(and
					(== (gIconBar curIcon?) (gIconBar at: 5))
					(not (gIconBar curInvIcon?))
				)
				(gIconBar advanceCurIcon:)
			)
		)
		(gGame setCursor: ((gIconBar curIcon?) cursor?) 1)
		(= gCursorNumber ((gIconBar curIcon?) cursor?))
	)
	
	(method (showAbout)
		((ScriptID ABOUT_SCRIPT 0) doit:)
		(DisposeScript ABOUT_SCRIPT)
	)
	
	(method (showControls &tmp temp0)
		(= temp0 ((gIconBar curIcon?) cursor?))
		((ScriptID GAMECONTROLS_SCRIPT 0) doit:)
		(gGameControls dispose:)
		(gGame setCursor: temp0 1)
	)
)

(instance icon0 of IconItem
	(properties
		view 990
		loop 0
		cel 0
		cursor 980
		type $5000
		message V_WALK
		signal $0041
		maskView 990
		maskLoop 13
		noun N_MOVEICON
		helpVerb V_HELP
	)
	
	(method (init)
		(= lowlightColor gLowlightColor)
		(super init:)
	)
	
	(method (select &tmp temp0)
		(return
			(if (super select: &rest)
				(gIconBar hide:)
				(return 1)
			else
				(return 0)
			)
		)
	)
)

(instance icon1 of IconItem
	(properties
		view 990
		loop 1
		cel 0
		cursor 981
		message V_LOOK
		signal $0041
		maskView 990
		maskLoop 13
		noun N_EXAMINEICON
		helpVerb V_HELP
	)
	
	(method (init)
		(= lowlightColor gLowlightColor)
		(super init:)
	)
)

(instance icon2 of IconItem
	(properties
		view 990
		loop 2
		cel 0
		cursor 982
		message V_DO
		signal $0041
		maskView 990
		maskLoop 13
		noun N_DOICON
		helpVerb V_HELP
	)
	
	(method (init)
		(= lowlightColor gLowlightColor)
		(super init:)
	)
)

(instance icon3 of IconItem
	(properties
		view 990
		loop 3
		cel 0
		cursor 983
		message V_TALK
		signal $0041
		maskView 990
		maskLoop 13
		maskCel 4
		noun N_TALKICON
		helpVerb V_HELP
	)
	
	(method (init)
		(= lowlightColor gLowlightColor)
		(super init:)
	)
)

; Use this icon for whatever action you want
(instance icon4 of IconItem
	(properties
		view 990
		loop 10         ; This is currently a loop with "empty" cels
		cel 0
		cursor 999      ; The cursor view associated with your action.
		message 0       ; The verb associated with this action.
		signal $0041
		maskView 990
		maskLoop 13
		maskCel 4
		noun 0          ; The noun for your button
		helpVerb V_HELP
	)
	
	(method (init)
		(= lowlightColor gLowlightColor)
		(super init:)
	)
)

(instance icon6 of IconItem
	(properties
		view 990
		loop 4
		cel 0
		cursor 999
		message 0
		signal $0041
		maskView 990
		maskLoop 13
		maskCel 4
		noun N_INVENTORYICON
		helpVerb V_HELP
	)
	
	(method (init)
		(= lowlightColor gLowlightColor)
		(super init:)
	)
	
	(method (select param1 &tmp newEvent temp1 currentInvIcon temp3 temp4)
		(return
			(cond 
				((& signal icDISABLED) 0)
				((and argc param1 (& signal notUpd))
					(= currentInvIcon (gIconBar curInvIcon?))
					(if currentInvIcon
						(= temp3
							(+
								(/
									(-
										(- nsRight nsLeft)
										(CelWide (currentInvIcon view?) 2 (currentInvIcon cel?))
									)
									2
								)
								nsLeft
							)
						)
						(= temp4
							(+
								(gIconBar y?)
								(/
									(-
										(- nsBottom nsTop)
										(CelHigh (currentInvIcon view?) 2 (currentInvIcon cel?))
									)
									2
								)
								nsTop
							)
						)
					)
					(= temp1 1)
					(DrawCel view loop temp1 nsLeft nsTop -1)
					(= currentInvIcon (gIconBar curInvIcon?))
					(if currentInvIcon
						(DrawCel
							((= currentInvIcon (gIconBar curInvIcon?)) view?)
							2
							(currentInvIcon cel?)
							temp3
							temp4
							-1
						)
					)
					(Graph grUPDATE_BOX nsTop nsLeft nsBottom nsRight 1)
					(while (!= ((= newEvent (Event new:)) type?) 2)
						(newEvent localize:)
						(cond 
							((self onMe: newEvent)
								(if (not temp1)
									(= temp1 1)
									(DrawCel view loop temp1 nsLeft nsTop -1)
									(= currentInvIcon (gIconBar curInvIcon?))
									(if currentInvIcon
										(DrawCel
											((= currentInvIcon (gIconBar curInvIcon?)) view?)
											2
											(currentInvIcon cel?)
											temp3
											temp4
											-1
										)
									)
									(Graph grUPDATE_BOX nsTop nsLeft nsBottom nsRight 1)
								)
							)
							(temp1
								(= temp1 0)
								(DrawCel view loop temp1 nsLeft nsTop -1)
								(= currentInvIcon (gIconBar curInvIcon?))
								(if currentInvIcon
									(DrawCel
										((= currentInvIcon (gIconBar curInvIcon?)) view?)
										2
										(currentInvIcon cel?)
										temp3
										temp4
										-1
									)
								)
								(Graph grUPDATE_BOX nsTop nsLeft nsBottom nsRight 1)
							)
						)
						(newEvent dispose:)
					)
					(newEvent dispose:)
					(if (== temp1 1)
						(DrawCel view loop 0 nsLeft nsTop -1)
						(= currentInvIcon (gIconBar curInvIcon?))
						(if currentInvIcon
							(DrawCel
								((= currentInvIcon (gIconBar curInvIcon?)) view?)
								2
								(currentInvIcon cel?)
								temp3
								temp4
								-1
							)
						)
						(Graph grUPDATE_BOX nsTop nsLeft nsBottom nsRight 1)
					)
					temp1
				)
				(else 1)
			)
		)
	)
)

(instance icon7 of IconItem
	(properties
		view 990
		loop 5
		cel 0
		cursor 999
		type $0000
		message 0
		signal $0043
		maskView 990
		maskLoop 13
		noun N_SELECTINVICON2
		helpVerb V_HELP
	)
	
	(method (init)
		(= lowlightColor gLowlightColor)
		(super init:)
	)
	
	(method (select &tmp theGCursorNumber)
		(return
			(if (super select: &rest)
				(gIconBar hide:)
				(= theGCursorNumber gCursorNumber)
				(gInv showSelf: gEgo)
				(gGame setCursor: theGCursorNumber 1)
				(return 1)
			else
				(return 0)
			)
		)
	)
)

(instance icon8 of IconItem
	(properties
		view 990
		loop 7
		cel 0
		cursor 999
		message V_COMBINE
		signal $0043
		maskView 990
		maskLoop 13
		noun N_SETTINGSICON
		helpVerb V_HELP
	)
	
	(method (init)
		(= lowlightColor gLowlightColor)
		(super init:)
	)
	
	(method (select)
		(return
			(if (super select: &rest)
				(gIconBar hide:)
				(gGame showControls:)
				(return 1)
			else
				(return 0)
			)
		)
	)
)

(instance icon9 of IconItem
	(properties
		view 990
		loop 9
		cel 0
		cursor 989
		type evHELP
		message V_HELP
		signal $0003
		maskView 990
		maskLoop 13
		noun N_HELPICON
		helpVerb V_HELP
	)
	
	(method (init)
		(= lowlightColor gLowlightColor)
		(if gDialog (gDialog dispose:))
		(super init:)
	)
)

(instance checkIcon of Code
	(properties)
	
	(method (doit param1)
		(if
			(and
				(param1 isKindOf: IconItem)
				(& (param1 signal?) $0004)
			)
			(= gCheckedIcons
				(| gCheckedIcons (>> $8000 (gIconBar indexOf: param1)))
			)
		)
	)
)

(instance lb2DoVerbCode of Code
	(properties)
	
	(method (doit theVerb param2)
		(if (User canControl:)
			(if (== param2 gEgo)
				(if (Message msgSIZE 0 N_EGO theVerb 0 1)
					(gMessager say: N_EGO theVerb 0 0 0 0)
				else
					(gMessager say: N_EGO 0 0 (Random 1 2) 0 0)
				)
			else
				(switch theVerb
					(V_DO
						(gMessager say: 0 V_DO 0 (Random 1 2) 0 0)
					)
					(V_TALK
						(gMessager say: 0 V_TALK 0 (Random 1 2) 0 0)
					)
					(else 
						(if (not (IsOneOf theVerb V_LOOK))
							(gMessager say: 0 V_COMBINE 0 (Random 2 3) 0 0)
						)
					)
				)
			)
		)
	)
)

(instance lb2FtrInit of Code
	(properties)
	
	(method (doit param1)
		(if (== (param1 sightAngle?) $6789)
			(param1 sightAngle: 90)
		)
		(if (== (param1 actions?) $6789) (param1 actions: 0))
		(if
			(and
				(not (param1 approachX?))
				(not (param1 approachY?))
			)
			(param1 approachX: (param1 x?) approachY: (param1 y?))
		)
	)
)

; This converts verbs into a bit flag mask
(instance lb2ApproachCode of Code
	(properties)
	
	(method (doit param1)
		(switch param1
			(V_LOOK 1)
			(V_TALK 2)
			(V_WALK 4)
			(V_DO 8)
			; Add other verbs here, with doubling numbers.
;
;            (31
;                16
;            )
;            (29
;                64
;            )
;            (25
;                128
;            )
			(else  $8000)
		)
	)
)

(instance mainWindow of BorderWindow
	(properties)
)

(instance templateNarrator of Narrator
	(properties)
	
	(method (init)
		(= font gFont)
		(self back: gColorWindowBackground)
		(super init: &rest)
	)
)

(instance testMessager of Messager
	(properties)
	
	(method (findTalker talkerNumber &tmp temp0)
		(= gCurrentTalkerNumber talkerNumber)
		(= temp0
			(switch talkerNumber
				(NARRATOR gNarrator)
			))
		; Add more cases here for different narrators
		; (8
		; (ScriptID 109 7)
		; )
		(if temp0
			(if (not (temp0 isKindOf: Narrator))
				(Prints {Invalid talker.})
			)
			(return temp0)
		else
			(return (super findTalker: talkerNumber))
		)
	)
)
