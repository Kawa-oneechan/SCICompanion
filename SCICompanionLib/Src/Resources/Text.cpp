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
#include "stdafx.h"
#include "Text.h"
#include "ResourceEntity.h"
#include "AppState.h"

using namespace std;

std::string AutoGenTextSentinel = "_AUTOGEN_";

//From DOS-437 to Win-1252
const unsigned char dos2winTable[] = {
		  //  0     1     2     3     4     5     6     7     8     9     A     B     C     D     E     F
	/* 80 */ 0xC7, 0xFC, 0xE9, 0xE2, 0xE4, 0xE0, 0xE5, 0xE7, 0xEA, 0xEB, 0xE8, 0xEF, 0xEE, 0xEC, 0xC4, 0xC5,
	/* 90 */ 0xC9, 0xE6, 0xC6, 0xF4, 0xF6, 0xF2, 0xFB, 0xF9, 0xFF, 0xD6, 0xDC, 0xA2, 0xA3, 0xA5, 0x3F, 0x3F,
	/* A0 */ 0xE1, 0xED, 0xF3, 0xFA, 0xF1, 0xD1, 0xAA, 0xBA, 0xBF, 0x3F, 0xAC, 0xBD, 0xBC, 0xA1, 0xAB, 0xBB,
	/* B0 */ 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F,
	/* C0 */ 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F,
	/* D0 */ 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F,
	/* E0 */ 0x3F, 0xDF, 0x3F, 0x3F, 0x3F, 0x3F, 0xB5, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F,
	/* F0 */ 0x3F, 0xB1, 0x3F, 0x3F, 0x3F, 0x3F, 0xF7, 0x3F, 0xB0, 0x3F, 0xB7, 0x3F, 0x3F, 0xB2, 0x3F, 0x3F,
};

//From Win-1252 to DOS-437
const unsigned char win2dosTable[] = {
		  //  0     1     2     3     4     5     6     7     8     9     A     B     C     D     E     F
	/* A0 */ 0x3F, 0xAD, 0x9B, 0x9C, 0x3F, 0x9D, 0x3F, 0x3F, 0x3F, 0x3F, 0xA6, 0xAE, 0xAA, 0x3F, 0x3F, 0x3F,
	/* B0 */ 0xF8, 0xF1, 0xFD, 0x3F, 0x3F, 0xE6, 0x3F, 0xFA, 0x3F, 0x3F, 0xA7, 0xBB, 0xAC, 0xAB, 0x3F, 0xA8,
	/* C0 */ 0x3F, 0x3F, 0x3F, 0x3F, 0x8E, 0x8F, 0x92, 0x80, 0x3F, 0x90, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F,
	/* D0 */ 0x3F, 0xA5, 0x3F, 0x3F, 0x3F, 0x3F, 0x99, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x9A, 0x3F, 0x3F, 0xE1,
	/* E0 */ 0x85, 0xA0, 0x83, 0x3F, 0x84, 0x86, 0x91, 0x87, 0x8A, 0x82, 0x88, 0x89, 0x8D, 0xA1, 0x8C, 0x8B,
	/* F0 */ 0x3F, 0xA4, 0x95, 0xA2, 0x93, 0x3F, 0x94, 0xF6, 0x3F, 0x97, 0xA3, 0x96, 0x81, 0x3F, 0x3F, 0x98,
};

//////												OLD CODE
//////From DOS-437 to Win-1252
////const unsigned char dos2winTable[] = {
////	/* 80 */ 0xC7, 0xFC, 0xE9, 0xE2, 0xE4, 0xE0, 0xE5, 0xE7, 0xEA, 0xEB, 0xE8, 0xEF, 0xEE, 0xEC, 0xC4, 0xC5,
////	/* 90 */ 0xC9, 0xE6, 0xC6, 0xF4, 0xF6, 0xF2, 0xFB, 0xF9, 0xFF, 0xD6, 0xDC, 0xA2, 0xA3, 0xA5, 0x3F, 0x3F,
////	/* A0 */ 0xE1, 0xED, 0xF3, 0xFA, 0xF1, 0xD1, 0xAA, 0xBA, 0xBF, 0x3F, 0xAC, 0xBD, 0xBC, 0xA1, 0xAB, 0xBB,
////};
////
//////From Win-1252 to DOS-437
////const unsigned char win2dosTable[] = {
////	/* C0 */ 0x3F, 0x3F, 0x3F, 0x3F, 0x8E, 0x8F, 0x3F, 0x80, 0x3F, 0x90, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F,
////	/* D0 */ 0x3F, 0xA5, 0x3F, 0x3F, 0x3F, 0x3F, 0x99, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x9A, 0x3F, 0x3F, 0x3F,
////	/* E0 */ 0x85, 0xA0, 0x83, 0x3F, 0x84, 0x86, 0x91, 0x87, 0x8A, 0x82, 0x88, 0x89, 0x8D, 0xA1, 0x8C, 0x8B,
////	/* F0 */ 0x3F, 0xA4, 0x95, 0xA2, 0x93, 0x3F, 0x94, 0x3F, 0x3F, 0x97, 0xA3, 0x96, 0x81, 0x3F, 0x3F, 0x98,
////};
////
//////Wait, where's 0xE1/U+00DF, the Sharp S? It's so separate from the rest that I can't be arsed, and just hardcode it.


/***************************************************************************
	Copyright (c) 2025 David R. (L@Zar0)

	This an addendum to the codepage 850, used in Western Europe.
	This helps for the conversion characters for specific european
	languages like Spanish, Italian, French, Dutch, German...

	This follow the same license as the main license of the code.
***************************************************************************/
//From DOS-850 to Win-1252
const unsigned char dos2winTable850[] = {
		  //  0     1     2     3     4     5     6     7     8     9     A     B     C     D     E     F
	/* 80 */ 0xC7, 0xFC, 0xE9, 0xE2, 0xE4, 0xE0, 0xE5, 0xE7, 0xEA, 0xEB, 0xE8, 0xEF, 0xEE, 0xEC, 0xC4, 0xC5,
	/* 90 */ 0xC9, 0xE6, 0xC6, 0xF4, 0xF6, 0xF2, 0xFB, 0xF9, 0xFF, 0xD6, 0xDC, 0xF8, 0xA3, 0xD8, 0xD7, 0x3F,
	/* A0 */ 0xE1, 0xED, 0xF3, 0xFA, 0xF1, 0xD1, 0xAA, 0xBA, 0xBF, 0xAE, 0xAC, 0xBD, 0xBC, 0xA1, 0xAB, 0xBB,
	/* B0 */ 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0xC1, 0xC2, 0xC0, 0xA9, 0x3F, 0x3F, 0x3F, 0x3F, 0xA2, 0xA5, 0x3F,
	/* C0 */ 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0xE3, 0xC3, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0xA4,
	/* D0 */ 0xF0, 0xD0, 0xCA, 0xCB, 0xC8, 0x3F, 0xCD, 0xCE, 0xCF, 0x3F, 0x3F, 0x3F, 0x3F, 0xA6, 0xCC, 0x3F,
	/* E0 */ 0xD3, 0xDF, 0xD4, 0xD2, 0xF5, 0xD5, 0xB5, 0xFE, 0xDE, 0xDA, 0xDB, 0xD9, 0xFD, 0xDD, 0xAF, 0xB4,
	/* F0 */ 0x3F, 0xB1, 0x3F, 0xBE, 0xB6, 0xA7, 0xF7, 0xB8, 0xB0, 0xA8, 0xB7, 0xB9, 0xB3, 0xB2, 0x3F, 0xA0,
};

//From Win-1252 to DOS-850
const unsigned char win2dosTable850[] = {
		  //  0     1     2     3     4     5     6     7     8     9     A     B     C     D     E     F
	/* A0 */ 0xFF, 0xAD, 0xBD, 0x9C, 0xCF, 0xBE, 0xDD, 0xF5, 0xF9, 0xB8, 0xA6, 0xAE, 0xAA, 0x3F, 0xA9, 0xEE,
	/* B0 */ 0xF8, 0xF1, 0xFD, 0xFC, 0xEF, 0x8F, 0xF4, 0xFA, 0xF7, 0xFB, 0xA7, 0xAF, 0xAC, 0xAB, 0xF3, 0xA8,
	/* C0 */ 0xB7, 0xB5, 0xB6, 0xC7, 0x8E, 0xE6, 0x92, 0x80, 0xD4, 0x90, 0xD2, 0xD3, 0xDE, 0xD6, 0xD7, 0xD8,
	/* D0 */ 0xD1, 0xA5, 0xE3, 0xE0, 0xE2, 0xE5, 0x99, 0x9E, 0x9D, 0xEB, 0xE9, 0xEA, 0x9A, 0xED, 0xE8, 0xE1,
	/* E0 */ 0x85, 0xA0, 0x83, 0xC6, 0x84, 0x86, 0x91, 0x87, 0x8A, 0x82, 0x88, 0x89, 0x8D, 0xA1, 0x8C, 0x8B,
	/* F0 */ 0xD0, 0xA4, 0x95, 0xA2, 0x93, 0xE4, 0x94, 0xF6, 0x9B, 0x97, 0xA3, 0x96, 0x81, 0xEC, 0xE7, 0x98,
};

unsigned char Dos2WinChar(unsigned char uDosChar)
{
	int intCP;

	intCP = appState->GetResourceMap().Helper().GetCodepage();

	switch (intCP)
	{
		case 850:
			return (uDosChar >= 0x80) ? dos2winTable850[uDosChar - 0x80] : uDosChar;
			break;

		case 437:
			return (uDosChar >= 0x80) ? dos2winTable[uDosChar - 0x80] : uDosChar;
			break;

		default:
			return uDosChar;
			break;
	}
}

std::string Dos2Win(std::string &str)
{
	int intCP;

	intCP = appState->GetResourceMap().Helper().GetCodepage();

	if (intCP == 1252)
		return str;
	std::string ret;

	// Let's work with more than one codepage for the conversion
	// Codepage 437: United States
	// Codepage 850: Western Europe (Mainly Spain, Italy, France, German...)

	switch (intCP)
	{
		case 850:
			for (size_t i = 0; i < str.length(); i++)
			{
				if ((unsigned char)str[i] > 0x7F)
					ret.push_back(dos2winTable850[(unsigned char)str[i] - 0x80]);
				else
					ret.push_back(str[i]);
			}
			break;

		case 437:
		default:
			for (size_t i = 0; i < str.length(); i++)
			{
				if ((unsigned char)str[i] > 0x7F)
					ret.push_back(dos2winTable[(unsigned char)str[i] - 0x80]);
				else
					ret.push_back(str[i]);
			}
			break;
	}

	//for (size_t i = 0; i < str.length(); i++)
	//{
	//	if ((unsigned char)str[i] == 0xE1) ret.push_back(0xDFu); //sharp s
	//	else if ((unsigned char)str[i] >= 0x80 && (unsigned char)str[i] <= 0xAF)
	//		ret.push_back(dos2winTable[(unsigned char)str[i] - 0x80]);
	//	else
	//		ret.push_back(str[i]);
	//}
	return ret;
}

std::string Win2Dos(const std::string &str)
{
	int intCP = appState->GetResourceMap().Helper().GetCodepage();

	if (intCP == 1252)
		return str;
	std::string ret;

	switch (intCP)
	{
	case 850:
		for (size_t i = 0; i < str.length(); i++)
		{
			if ((unsigned char)str[i] > 0x9F)
				ret.push_back(win2dosTable850[(unsigned char)str[i] - 0xA0]);
			else
				ret.push_back(str[i]);
		}
		break;

	case 437:
	default:
		for (size_t i = 0; i < str.length(); i++)
		{
			if ((unsigned char)str[i] > 0x9F)
				ret.push_back(win2dosTable[(unsigned char)str[i] - 0xA0]);
			else
				ret.push_back(str[i]);
		}
		break;
	}

	//for (size_t i = 0; i < str.length(); i++)
	//{
	//	if ((unsigned char)str[i] == 0xDF) ret.push_back(0xE1u); //sharp s
	//	else if ((unsigned char)str[i] >= 0xC0 && (unsigned char)str[i] <= 0xFF)
	//		ret.push_back(win2dosTable[(unsigned char)str[i] - 0xC0]);
	//	else
	//		ret.push_back(str[i]);
	//}
	return ret;
}

bool TextEntry::operator == (const TextEntry &other) const
{
	return Noun == other.Noun &&
		Verb == other.Verb &&
		Condition == other.Condition &&
		Sequence == other.Sequence &&
		Talker == other.Talker &&
		Reference == other.Reference &&
		Text == other.Text &&
		Talker == other.Talker;
}
bool TextEntry::operator!=(const TextEntry &other) const
{
	return !(*this == other);
}

int TextComponent::AddString(const std::string &theString)
{
	TextEntry entry = { 0 };
	entry.Text = theString;
	Texts.push_back(entry);
	return (int)(Texts.size() - 1); // Index of added string
}

int TextComponent::AddStringDedupe(const std::string &theString)
{
	for (size_t i = 0; i < Texts.size(); i++)
	{
		if (Texts[i].Text == theString)
		{
			// This string already exists.  Just re-use it.
			return (int)i;
		}
	}
	return AddString(theString);
}

TextChangeHint TextComponent::SetStringAt(int iIndex, const std::string &theString)
{
	TextChangeHint hint = (theString != Texts[iIndex].Text) ? TextChangeHint::Changed : TextChangeHint::None;
	Texts[iIndex].Text = theString;
	return hint;
}

TextChangeHint TextComponent::MoveStringUp(int iIndex)
{
	TextChangeHint hint = TextChangeHint::None;
	if ((iIndex > 0) && (iIndex < (int)Texts.size()))
	{
		hint = TextChangeHint::Changed;
		std::swap(Texts[iIndex - 1], Texts[iIndex]);
	}
	return hint;
}

TextChangeHint TextComponent::MoveStringDown(int iIndex)
{
	TextChangeHint hint = TextChangeHint::None;
	if (Texts.size() > 1)
	{
		if (iIndex < (int)(Texts.size() - 1))
		{
			hint = TextChangeHint::Changed;
			std::swap(Texts[iIndex + 1], Texts[iIndex]);
		}
	}
	return hint;
}

TextChangeHint TextComponent::DeleteString(int iIndex)
{
	Texts.erase(Texts.begin() + iIndex);
	return TextChangeHint::Changed;
}

// ILookupNames
std::string TextComponent::Lookup(uint16_t wName) const
{
	std::string ret;
	if (wName < Texts.size())
	{
		ret = Texts[wName].Text;
	}
	return ret;
}

bool TextComponent::AreTextsEqual(const TextComponent &other) const
{
	bool maybeEqual = other.Texts.size() == Texts.size();
	for (size_t i = 0; maybeEqual && (i < Texts.size()); i++)
	{
		maybeEqual = (Texts[i] == other.Texts[i]);
	}
	return maybeEqual;
}

bool TextComponent::WasAutoGenerated() const
{
	for (const TextEntry &text : Texts)
	{
		if (text.Text == AutoGenTextSentinel)
		{
			return true;
		}
	}
	return false;
}

void TextWriteTo(const ResourceEntity &resource, sci::ostream &byteStream, std::map<BlobKey, uint32_t> &propertyBag)
{
	const TextComponent &text = resource.GetComponent<TextComponent>();
	// Note: this function is not unicode aware
	for (size_t i = 0; i < text.Texts.size(); i++)
	{
		const string &str = Win2Dos(text.Texts[i].Text);
		byteStream.WriteBytes((uint8_t*)str.c_str(), (int)str.length() + 1);
	}
}

void TextReadFrom(ResourceEntity &resource, sci::istream &byteStream, const std::map<BlobKey, uint32_t> &propertyBag)
{
	TextComponent &text = resource.GetComponent<TextComponent>();
	assert(text.Texts.empty());
	text.Flags = MessagePropertyFlags::None;
	// Catch our own exceptions.
	try
	{
		while (byteStream.has_more_data())
		{
			string str;
			byteStream >> str;
			if (byteStream.good())
			{
				TextEntry entry = { 0 };				
				entry.Text = Dos2Win(str);
				text.Texts.push_back(entry);
			}
		}
	}
	catch (...) {}
}

ResourceTraits textTraits =
{
	ResourceType::Text,
	&TextReadFrom,
	&TextWriteTo,
	&NoValidationFunc,
	nullptr
};

ResourceEntity *CreateTextResource(SCIVersion version)
{
	std::unique_ptr<ResourceEntity> pResource = std::make_unique<ResourceEntity>(textTraits);
	pResource->AddComponent(move(make_unique<TextComponent>()));
	return pResource.release();
}

ResourceEntity *CreateDefaultTextResource(SCIVersion version)
{
	// Nothing different.
	return CreateTextResource(version);
}
