;;; Sierra Script 1.0 - (do not remove this comment)
(script# TITLEROOM_SCRIPT)
(include sci.sh)
(include Verbs.sh)
(include 0.shm)
(include game.sh)
(use Main)
(use Controls)
(use Print)
(use Cycle)
(use Game)
(use Actor)
(use System)

(public
	rm100 0
)

(instance myDialog of Dialog
	(properties)
)

(instance rm100 of Room
	(properties
		picture 100
	)
	
	(method (init)
		(if gDialog (gDialog dispose:))
		(super init:)
		(gOldMH addToFront: self)
		(gOldKH addToFront: self)
		(gGame setCursor: 996 1)
		(gIconBar hide: disable:)
		(gUser canInput: TRUE)
		(self setScript: rmScript)
	)
	
	(method (dispose)
		(gIconBar hide: enable:)
		(= gNormalCursor 999)
		(gGame setCursor: 996 1)
		(gOldKH delete: self)
		(gOldMH delete: self)
		(super dispose: &rest)
	)
	
	(method (handleEvent pEvent)
		(if
			(and
				(!= (rmScript state?) 1)
				(&
					(pEvent type?)
					(| evVERB evMOUSEBUTTON evMOUSERELEASE evKEYBOARD)
				)
			)
			; Skip to state 4 if the keyboard or mouse is used
			(rmScript changeState: 1)
			(pEvent claimed: TRUE)
			(return)
		else
			(super handleEvent: pEvent)
		)
	)
)

(instance rmScript of Script
	(properties)
	
	(method (changeState newState &tmp theChoice)
		(switch (= state newState)
			(0 (= seconds 4))
			; Wait 4 seconds before going to the next state.
			(1
				(= seconds 0)				
				(= gNormalCursor 999)
				(gGame setCursor: 999 1)
				(= theChoice
					(Print
						dialog: myDialog
						width: 150
						font: 4
						addButton: 0 N_TITLEMENU V_LOOK 0 1 0 0 0
						addButton: 1 N_TITLEMENU V_LOOK 0 2 0 16 0
						addButton: 2 N_TITLEMENU V_LOOK 0 3 0 32 0
						init:
					)
				)
				(switch theChoice
					(0 (gRoom newRoom: 110))
					(1
						(gGame restore:)
						(self changeState: state)
					)
					(2 (= gQuitGame TRUE))
				)
			)
		)
	)
)
