/***************************************************************************
	Copyright (c) 2020 Philip Fortier

	This program is free software; you can redistribute it and/or
	modify it under the terms of the GNU General Public License
	as published by the Free Software Foundation; either version 2
	of the License, or (at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.
***************************************************************************/
#pragma once

#include "Components.h"
#include "Audio.h"
#include "AudioProcessingSettings.h"

// Contains an original 16-bit recording (the audio "negative")
// along with some settings.
// This does not correspond to any Sierra resource. It's simply used in our audio cache files.
// The persisted form of this is a wav file.
struct AudioNegativeComponent : public ResourceComponent
{
public:
	AudioNegativeComponent() {}

	ResourceComponent *Clone() const override
	{
		return new AudioNegativeComponent(*this);
	}

	AudioComponent Audio;

	AudioProcessingSettings Settings;
};
