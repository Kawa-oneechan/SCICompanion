;;; Sierra Script 1.0 - (do not remove this comment)
;	
;	 This script contains the in-game debug functionality, triggered by pressing ALT-d.
(script# INGAME_DEBUG_SCRIPT)
(include sci.sh)
(include game.sh)
(use Main)
(use Controls)
(use Print)
(use Feature)
(use SysWindow)
(use User)
(use Actor)
(use System)
(use InventoryItem)
(use DialogControls)

(public
	debugHandler 0
)

(local
	[local0 27]
	; For inventory dialog:
	newDButton
	[local1 2]
)
(procedure (localproc_0052)
	(if
		(IsOneOf
			(gRoom style?)
			dpOPEN_SCROLL_RIGHT
			dpOPEN_SCROLL_LEFT
			dpOPEN_SCROLL_UP
			dpOPEN_SCROLL_DOWN
		)
		(gRoom
			drawPic: (gRoom picture?) dpOPEN_NO_TRANSITION
			style: dpOPEN_NO_TRANSITION
		)
	)
)

(instance debugHandler of Feature
	(properties)
	
	(method (init)
		(super init:)
		(gOldMH addToFront: self)
		(gOldKH addToFront: self)
	)
	
	(method (dispose)
		(gOldMH delete: self)
		(gOldKH delete: self)
		(super dispose:)
		(DisposeScript INGAME_DEBUG_SCRIPT)
	)
	
	(method (handleEvent pEvent &tmp [temp0 160] temp160 newEvent gOldCastFirst theGFont temp164 temp165 temp166 temp167 temp168 temp169 temp170 temp171 temp172 userAlterEgo temp174 temp175 temp176 temp177 temp178)
		(return
			(switch (pEvent type?)
				(evKEYBOARD
					(pEvent claimed: TRUE)
					(switch (pEvent message?)
						(KEY_ALT_a
							; Show cast
							(= gOldCastFirst (gCast first:))
							(while gOldCastFirst
								(= temp164 (NodeValue gOldCastFirst))
								(Format
									@temp0
									10
									1
									((temp164 -super-?) name?)
									(temp164 view?)
									(temp164 loop?)
									(temp164 cel?)
									(temp164 x?)
									(temp164 y?)
									(temp164 z?)
									(temp164 heading?)
									(temp164 priority?)
									(temp164 signal?)
									(if (temp164 isKindOf: Actor)
										(temp164 illegalBits?)
									else
										-1
									)
								)
								(breakif
									(not
										(Print
											addText:
												@temp0
												(CelWide
													(temp164 view?)
													(temp164 loop?)
													(temp164 cel?)
												)
												0
											window: SysWindow
											addTitle: (temp164 name?)
											addIcon: (temp164 view?) (temp164 loop?) (temp164 cel?) 0 0
											init:
										)
									)
								)
								(= gOldCastFirst (gCast next: gOldCastFirst))
							)
						)
						(KEY_ALT_y
							; Show polygons
							(= temp160 (gRoom obstacles?))
							(if temp160
								(temp160 eachElementDo: #perform drawPoly)
								(Graph grUPDATE_BOX 0 0 190 320 VISUAL)
							)
						)
						(KEY_ALT_c
							; Control screen
							(localproc_0052)
							(Show CONTROL)
						)
						(KEY_ALT_e
							; Show ego info
							(Format
								@temp0
								10
								2
								(gEgo name?)
								(gEgo view?)
								(gEgo loop?)
								(gEgo cel?)
								(gEgo x?)
								(gEgo y?)
								(gEgo z?)
								(gEgo heading?)
								(gEgo priority?)
								(gEgo signal?)
								(gEgo illegalBits?)
								(gEgo onControl:)
								(gEgo onControl: 1)
							)
							(Print
								addText: @temp0
								addIcon: (gEgo view?) (gEgo loop?) (gEgo cel?)
								init:
							)
						)
						(KEY_ALT_g
							; Set global
							(= temp0 0)
							(GetInput @temp0 6 {Variable No.})
							(= gOldCastFirst (ReadNumber @temp0))
							(= temp0 0)
							(GetInput @temp0 6 {Value})
							(= [gEgo gOldCastFirst] (ReadNumber @temp0))
							(= temp0 0)
						)
						(KEY_ALT_h
							; Show global
							(= temp0 0)
							(Print
								addText: {Global number:}
								addEdit: @temp0 6 0 12
								init:
							)
							(= gOldCastFirst (ReadNumber @temp0))
							(if (IsObject [gEgo gOldCastFirst])
								(Format
									@temp0
									{ Global %d: %s_}
									gOldCastFirst
									([gEgo gOldCastFirst] name?)
								)
							else
								(Format
									@temp0
									{ Global %d: %d_}
									gOldCastFirst
									[gEgo
									gOldCastFirst]
								)
							)
							(Prints @temp0)
						)
						(KEY_ALT_i (dInvD doit:))
						; Inventory selector
						(KEY_ALT_j
							; Show cast
							(= gOldCastFirst 0)
							(while (< gOldCastFirst (gCast size?))
								(= temp164 (gCast at: gOldCastFirst))
								(if (not (& (temp164 signal?) $0004))
									(Format
										@temp0
										10
										1
										((temp164 -super-?) name?)
										(temp164 view?)
										(temp164 loop?)
										(temp164 cel?)
										(temp164 x?)
										(temp164 y?)
										(temp164 z?)
										(temp164 heading?)
										(temp164 priority?)
										(temp164 signal?)
										(if (temp164 isKindOf: Actor)
											(temp164 illegalBits?)
										else
											-1
										)
									)
									(Print
										addText:
											@temp0
											(CelWide
												(temp164 view?)
												(temp164 loop?)
												(temp164 cel?)
											)
											0
										window: SysWindow
										addTitle: (temp164 name?)
										addIcon: (temp164 view?) (temp164 loop?) (temp164 cel?) 0 0
										init:
									)
								)
								(++ gOldCastFirst)
							)
						)
						(KEY_ALT_k
							; Show palette
							(= temp160 (GetPort))
							(SetPort 0)
							(= temp171 5)
							(= temp172 16)
							(= temp167 15)
							(= temp168 80)
							(= temp170 (+ temp167 (* 34 temp171)))
							(= temp169 (+ temp168 (* 10 temp172)))
							(= temp165
								(Graph grSAVE_BOX temp167 temp168 temp170 temp169 1)
							)
							(Graph grFILL_BOX temp167 temp168 temp170 temp169 1 255)
							(= temp166 0)
							(while (< temp166 256)
								(Graph
									grFILL_BOX
									(+ temp167 temp171 (* temp171 (/ temp166 8)))
									(+ temp168 temp172 (* 16 (mod temp166 8)))
									(+ temp167 temp171 temp171 (* temp171 (/ temp166 8)))
									(+ temp168 temp172 temp172 (* temp172 (mod temp166 8)))
									1
									temp166
								)
								(++ temp166)
							)
							(Graph grUPDATE_BOX temp167 temp168 temp170 temp169 1)
							(repeat
								(= newEvent (Event new:))
								(breakif
									(or (== (newEvent type?) 1) (== (newEvent type?) 4))
								)
								(newEvent dispose:)
							)
							(newEvent dispose:)
							(Graph grRESTORE_BOX temp165)
							(Graph grUPDATE_BOX temp167 temp168 temp170 temp169 1)
							(SetPort temp160)
						)
						(KEY_ALT_l
							; Set flag
							(= temp0 0)
							(= gOldCastFirst (GetNumber {Flag No.}))
							(Bset gOldCastFirst)
						)
						(KEY_ALT_m
							; Clear flag
							(= temp0 0)
							(= gOldCastFirst (GetNumber {Flag No.}))
							(Bclear gOldCastFirst)
						)
						(KEY_ALT_n
							; Show flag
							(= temp0 0)
							(= gOldCastFirst (GetNumber {Flag No.}))
							(if (Btest gOldCastFirst)
								(Prints {TRUE})
							else
								(Prints {FALSE})
							)
						)
						(KEY_ALT_p
							; Priority screen
							(localproc_0052)
							(Show PRIORITY)
						)
						(KEY_ALT_q
							; Detail level
							(gGame detailLevel: 1)
						)
						(KEY_ALT_r
							; Show room info
							(Format
								@temp0
								10
								3
								(gRoom name?)
								gRoomNumber
								(gRoom curPic?)
								(gRoom style?)
								(gRoom horizon?)
								(gRoom north?)
								(gRoom south?)
								(gRoom east?)
								(gRoom west?)
								(if (IsObject (gRoom script?))
									((gRoom script?) name?)
								else
									{..none..}
								)
							)
							(Print width: 120 addText: @temp0 init:)
							(gGame showMem:)
						)
						(KEY_ALT_s
							; Show message
							(= temp0 0)
							(if
								(Print
									addText: {Which Format?}
									addButton: 0 {String} 0 12
									addButton: 1 {Message} 50 12
									init:
								)
								(= temp174 (GetNumber {Noun?} 0))
								(= temp175 (GetNumber {Verb?} 0))
								(= temp176 (GetNumber {Case?} 0))
								(= temp177 (GetNumber {Sequence?} 0))
								(Message msgGET temp174 temp175 temp176 temp177 @temp0)
							else
								(GetInput @temp0 50 {String to display?})
							)
							(= temp167 (GetNumber {Y Parameter?} 0))
							(= temp168 (GetNumber {X Parameter?} 0))
							(= gOldCastFirst (GetNumber {Box Width?} 0))
							(= theGFont (GetNumber {Font Number?} 0))
							(if (not theGFont) (= theGFont gFont))
							(Print
								posn: temp168 temp167
								width: gOldCastFirst
								font: theGFont
								addText: @temp0
								init:
							)
						)
						(KEY_ALT_t
							; Teleport
							(if gDialog (gDialog dispose:))
							(Print
								addText: {Which room do you want?}
								addEdit: @temp0 6 115 35
								init:
							)
							(if
							(and temp0 (> (= gOldCastFirst (ReadNumber @temp0)) 0))
								(gRoom newRoom: gOldCastFirst)
							)
						)
						(KEY_ALT_u
							; Give hands on
							(User canInput: 1 canControl: 1)
							(gIconBar
								enable:
									ICONINDEX_WALK
									ICONINDEX_LOOK
									ICONINDEX_DO
									ICONINDEX_TALK
									ICONINDEX_CURITEM
									ICONINDEX_INVENTORY
							)
						)
						(KEY_ALT_x (= gQuitGame 1))
						; Quit
						(KEY_ALT_v
							; Visual screen
							(localproc_0052)
							(Show VISUAL)
						)
						(KEY_ALT_f
							; Feature outlines
							(= temp167 0)
							(while (< temp167 (gCast size?))
								(Graph
									grFILL_BOX
									((gCast at: temp167) brTop?)
									((gCast at: temp167) brLeft?)
									((gCast at: temp167) brBottom?)
									((gCast at: temp167) brRight?)
									1
									gColorWindowForeground
									-1
									-1
								)
								(++ temp167)
							)
						)
						(KEY_ALT_z (= gQuitGame 1))
						(KEY_QUESTION
							(Prints
								{Debug options:______(Page 1 of 5)\n\n___A - Show cast\n___B - Polygon editor\n___C - Show control map\n___D - Dialog editor\n___E - Show ego info\n___F - Show feature outlines\n___G - Set global\n}
							)
							(Prints
								{Debug options:______(Page 2 of 5)\n\n___H - Show global\n___I - Get inventory item\n___J - Justify text on screen\n___K - Show palette\n___L - Set flag\n___M - Clear flag\n___N - Show flag\n}
							)
							(Prints
								{Debug options:______(Page 3 of 5)\n\n___P - Show priority map\n___Q - Set Detail to 1\n___R - Show room info/free memory\n___S - Show a string or message\n___T - Teleport\n___U - Give HandsOn\n}
							)
							(Prints
								{Debug options:______(Page 4 of 5)\n\n___V - Show visual map\n___W - Feature writer\n___Y - View obstacles\n___X,Z - Quick quit\n}
							)
							(Prints
								{Debug options:______(Page 5 of 5)\n\n__A=Alt, C=Ctrl, L=Left shift, R=Right shift\n\n__Left click:\n____A_______Move ego\n____CL______Show ego\n____CR______Show room\n____CA______Show position\n}
							)
						)
						(else  (pEvent claimed: FALSE))
					)
				)
				(evMOUSEBUTTON
					(switch (pEvent modifiers?)
						((| emCTRL emALT)
							; Show mouse pos?
							(pEvent claimed: TRUE)
							(Format @temp0 10 4 (pEvent x?) (pEvent y?))
							(= temp160
								(Print
									posn: 160 10
									font: 999
									modeless: 1
									addText: @temp0
									init:
								)
							)
							(while
							(!= evMOUSERELEASE ((= newEvent (Event new:)) type?))
								(newEvent dispose:)
							)
							(newEvent dispose:)
							(temp160 dispose:)
						)
						((| emCTRL emRIGHT_SHIFT)
							(pEvent type: evKEYBOARD message: KEY_ALT_r)
							(self handleEvent: pEvent)
						)
						((| emCTRL emLEFT_SHIFT)
							(pEvent type: evKEYBOARD message: KEY_ALT_e)
							(self handleEvent: pEvent)
						)
						(emALT
							(pEvent claimed: TRUE)
							(= temp178 (gGame setCursor: 996))
							(= userAlterEgo (User alterEgo?))
							(= gOldCastFirst (userAlterEgo signal?))
							(userAlterEgo startUpd:)
							(while (!= 2 ((= newEvent (Event new:)) type?))
								(userAlterEgo x: (newEvent x?) y: (- (newEvent y?) 10))
								(Animate (gCast elements?) 0)
								(newEvent dispose:)
							)
							(newEvent dispose:)
							(gGame setCursor: temp178)
							(userAlterEgo signal: gOldCastFirst)
						)
					)
				)
			)
		)
	)
)

(instance drawPoly of Code
	(properties)
	
	(method (doit thePoly &tmp i x1 y1 x2 y2 thePoints index indexNext)
		(= thePoints (thePoly points?))
		(for ( (= i 0)) (< i (thePoly size?))  ( (++ i)) (= index (+ thePoints (* i 4))) (= x1 (Memory memPEEK index)) (= y1 (Memory memPEEK (+ index 2))) (= indexNext
			(+ thePoints (* (mod (+ i 1) (thePoly size?)) 4))
		) (= x2 (Memory memPEEK indexNext)) (= y2 (Memory memPEEK (+ indexNext 2))) (Graph grDRAW_LINE y1 x1 y2 x2 15 -1 -1))
	; TODO: Different colors for different types
	)
)

(instance dInvD of Dialog
	(properties)
	
	(method (init &tmp temp0 temp1 temp2 temp3 newDText curInvItem temp6)
		(= temp1 4)
		(= temp0 temp1)
		(= temp2 temp0)
		(= temp3 0)
		(= curInvItem (gInv first:))
		(while curInvItem
			(= temp6 (NodeValue curInvItem))
			(++ temp3)
			(if (temp6 isKindOf: InventoryItem)
				(= newDText (DText new:))
				(self
					add:
						(newDText
							value: temp6
							text: (temp6 name?)
							nsLeft: temp0
							nsTop: temp1
							state: 3
							font: gSmallFont
							setSize:
							yourself:
						)
				)
			)
			(if
			(< temp2 (- (newDText nsRight?) (newDText nsLeft?)))
				(= temp2 (- (newDText nsRight?) (newDText nsLeft?)))
			)
			(= temp1
				(+ temp1 (- (newDText nsBottom?) (newDText nsTop?)) 1)
			)
			(if (> temp1 140)
				(= temp1 4)
				(= temp0 (+ temp0 temp2 10))
				(= temp2 0)
			)
			(= curInvItem (gInv next: curInvItem))
		)
		(= window gWindow)
		(self setSize:)
		(= newDButton (DButton new:))
		(newDButton
			text: {All Done!}
			setSize:
			moveTo: (- nsRight (+ 4 (newDButton nsRight?))) nsBottom
		)
		(newDButton
			move: (- (newDButton nsLeft?) (newDButton nsRight?)) 0
		)
		(self add: newDButton setSize: center:)
		(return temp3)
	)
	
	(method (doit &tmp theNewDButton)
		(self init:)
		(self open: 4 15)
		(= theNewDButton newDButton)
		(repeat
			(= theNewDButton (super doit: theNewDButton))
			(breakif
				(or
					(not theNewDButton)
					(== theNewDButton -1)
					(== theNewDButton newDButton)
				)
			)
			(gEgo get: (gInv indexOf: (theNewDButton value?)))
		)
		(self eachElementDo: #dispose 1 dispose:)
	)
	
	(method (handleEvent pEvent &tmp pEventMessage pEventType)
		(= pEventMessage (pEvent message?))
		(switch (= pEventType (pEvent type?))
			(4
				(switch pEventMessage
					(KEY_UP (= pEventMessage 3840))
					(KEY_NUMPAD2
						(= pEventMessage 9)
					)
				)
			)
			(64
				(switch pEventMessage
					(JOY_UP
						(= pEventMessage 3840)
						(= pEventType 4)
					)
					(JOY_DOWN
						(= pEventMessage 9)
						(= pEventType 4)
					)
				)
			)
		)
		(pEvent type: pEventType message: pEventMessage)
		(super handleEvent: pEvent)
	)
)
