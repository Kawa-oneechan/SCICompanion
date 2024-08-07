;;; Sierra Script 1.0 - (do not remove this comment)
; This script contains the DebugPrint console functionality that works with SCI Companion.
(script# DEBUGOUT_SCRIPT)
(include sci.sh)
(include game.sh)
(use Main)
(use Controls)
(use User)
(use FileIO)
(use Game)

(public



	debugOut 0
)


(instance debugOut of File
	(properties
		; ndebug.log is a file that exists as open and unwriteable while the game is running.
		; This is how we detect when the game exits.
		name {ndebug.log}
	)
	
	(method (init initDebugFilename &tmp tempHandle [numberBuffer 10] startRoom tempRoom heapPointer)
		(= startRoom -1)
		; Open initDebugFilename to see if it has a script number in it. If so, we'll start in that room.
		(= tempHandle (FOpen initDebugFilename fOPENCREATE))
		(if tempHandle
			(if (!= (FGets @numberBuffer 10 tempHandle) -1)
				(= tempRoom (ReadNumber @numberBuffer))
				(if tempRoom
					; Check to see if export #0 of this script id
					(= heapPointer (ScriptID tempRoom 0))
					(if (IsObject heapPointer)
						(if (heapPointer isKindOf: Rm) (= startRoom tempRoom))
						; REVIEW: We only want to do this if it wasn't loaded before...
						(DisposeScript tempRoom)
					)
				)
			)
			(FClose tempHandle)
		)
		; Open ourselves for write
		(self open: fOPENFAIL)
		(return startRoom)
	)
	
	(method (debugPrint &tmp tempHandle theLength bufferPtr [buffer 100])
		; We open and close the file for each write, since that appears to be the only way to flush.
		(= tempHandle (FOpen {debug.log} fOPENFAIL))
		(if tempHandle
			(= theLength (FindFormatLen &rest))
			(if (<= theLength 100)
				(= bufferPtr @buffer)
				(Format bufferPtr &rest)
			else
				(= bufferPtr {Debug string too long.})
			)
			(FPuts tempHandle bufferPtr)
			(FPuts tempHandle {\n})
			(FClose tempHandle)
		)
	)
)

(procedure (FindFormatLen theText params &tmp temp0 temp1 temp2 temp3)
	(= temp1 (StrLen theText))
	(= temp0 temp1)
	(= temp2 0)
	(= temp3 0)
	(while (< temp3 temp1)
		(if (== (StrAt theText temp3) 37)   ; %
			(switch (StrAt theText (++ temp3))
				(KEY_d (= temp0 (+ temp0 5)))
				(KEY_x (= temp0 (+ temp0 4)))
				(KEY_s
					(= temp0 (+ temp0 (StrLen [params temp2])))
				)
			)
			(++ temp2)
		)
		(++ temp3)
	)
	(return (++ temp0))
)
