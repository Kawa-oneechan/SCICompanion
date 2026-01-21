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
#include "Message.h"
#include "Text.h"
#include "ResourceEntity.h"
#include "format.h"
#include "NounsAndCases.h"
#include "ResourceSourceFlags.h"

void ResolveReferences(TextComponent &messageComponent)
{
	int messageCount = messageComponent.Texts.size();
	for (int i = 0; i < messageCount; i++)
	{
		TextEntry message = messageComponent.Texts[i];
		uint8_t refN = (message.Reference >> 0) & 0xFF;
		uint8_t refV = (message.Reference >> 8) & 0xFF;
		uint8_t refC = (message.Reference >> 16) & 0xFF;
		uint8_t refS = (message.Reference >> 24) & 0xFF;
		if (refN + refV + refC != 0 && refS > 0)
		{
			messageComponent.Texts[i].Text = fmt::format("[REF {0:d}, {1:d}, {2:d}, {3:d}]", refN, refV, refC, refS);
			for (int j = 0; j < messageCount; j++)
			{
				TextEntry other = messageComponent.Texts[j];
				if (other.Noun == refN && other.Verb == refV && other.Condition == refC && other.Sequence == refS)
				{
					messageComponent.Texts[i].Text += fmt::format(" {0}", other.Text);
					break;
				}
			}
		}
	}
}

void UnresolveReferences(TextComponent &messageComponent)
{
	for (size_t i = 0; i < messageComponent.Texts.size(); i++)
	{
		TextEntry entry = messageComponent.Texts[i];
		messageComponent.Texts[i].Reference = 0; //assume it is not!
		if (entry.Text.substr(0, 5) == "[REF ")
		{
			char* lol = (char*)(entry.Text.c_str()) + 5;
			int refN = strtol(lol, &lol, 10);
			int refV = strtol(lol + 1, &lol, 10);
			int refC = strtol(lol + 1, &lol, 10);
			int refS = strtol(lol + 1, &lol, 10);
			uint32_t ref = (refN << 0) | (refV << 8) | (refC << 16) | (refS << 24);
			messageComponent.Texts[i].Reference = ref;
			messageComponent.Texts[i].Text = "";
		}
	}
}

void MessageReadFrom_2102(TextComponent &messageComponent, sci::istream &byteStream)
{
	uint16_t messageCount;
	byteStream >> messageCount;

	for (int i = 0; i < messageCount; i++)
	{
		TextEntry message = { 0 };
		byteStream >> message.Noun;
		byteStream >> message.Verb;
		uint16_t textOffset;
		byteStream >> textOffset;
		sci::istream textStream = byteStream;
		textStream.seekg(textOffset);
		textStream >> message.Text;

		message.Text = Dos2Win(message.Text);

		messageComponent.Texts.push_back(message);
	}
}

void MessageReadFrom_3411(TextComponent &messageComponent, sci::istream &byteStream)
{
	byteStream.skip(2);	 // ptr to first byte past text data (?)
	uint16_t messageCount;
	byteStream >> messageCount;

	for (int i = 0; i < messageCount; i++)
	{
		TextEntry message = { 0 };
		byteStream >> message.Noun;
		byteStream >> message.Verb;
		byteStream >> message.Condition;
		byteStream >> message.Sequence;
		byteStream >> message.Talker;
		uint16_t textOffset;
		byteStream >> textOffset;
		byteStream.skip(3); // Unknown?
		sci::istream textStream = byteStream;
		textStream.seekg(textOffset);
		textStream >> message.Text;

		message.Text = Dos2Win(message.Text);

		messageComponent.Texts.push_back(message);
	}
}

void MessageReadFrom_4000(TextComponent &messageComponent, sci::istream &byteStream)
{
	uint16_t offsetToAfterResource;
	byteStream >> offsetToAfterResource;
	byteStream >> messageComponent.MysteryNumber;

	uint16_t messageCount;
	byteStream >> messageCount;

	// "MysteryNumber" is a number that is roughly the number of messages, but sometimes a little more. Occasionally a lot more.
	// Sometimes its zero (KQ6, 95 and 916)

	for (int i = 0; i < messageCount; i++)
	{
		TextEntry message = { 0 };
		byteStream >> message.Noun;
		byteStream >> message.Verb;
		byteStream >> message.Condition;
		byteStream >> message.Sequence;
		byteStream >> message.Talker;
		uint16_t textOffset;
		byteStream >> textOffset;
		byteStream >> message.Reference;
		sci::istream textStream = byteStream;
		textStream.seekg(textOffset);
		textStream >> message.Text;

		message.Text = Dos2Win(message.Text);

		messageComponent.Texts.push_back(message);
	}

	ResolveReferences(messageComponent);
}

void MessageWriteTo_2102(const TextComponent &messageComponent, sci::ostream &byteStream)
{
	//UNTESTED
	byteStream.WriteWord((uint16_t)messageComponent.Texts.size());

	uint16_t textOffset = (uint16_t)(byteStream.tellp() + (4) * messageComponent.Texts.size());
	for (const TextEntry &entry : messageComponent.Texts)
	{
		byteStream << entry.Noun;
		byteStream << entry.Verb;
		byteStream << textOffset;
		textOffset += (uint16_t)(entry.Text.length() + 1);
	}

	for (const TextEntry &entry : messageComponent.Texts)
	{
		byteStream << Win2Dos(entry.Text);
	}

	uint16_t totalSize = (uint16_t)byteStream.tellp();
}

void MessageWriteTo_3411(const TextComponent &messageComponent, sci::ostream &byteStream)
{
	uint32_t fillThisIn = byteStream.tellp();
	byteStream.WriteWord(0); // We'll fill this in later
	uint32_t startCount = byteStream.tellp();
	byteStream.WriteWord((uint16_t)messageComponent.Texts.size());

	uint16_t textOffset = (uint16_t)(byteStream.tellp() + (5 + 2 + 3) * messageComponent.Texts.size());
	for (const TextEntry &entry : messageComponent.Texts)
	{
		byteStream << entry.Noun;
		byteStream << entry.Verb;
		byteStream << entry.Condition;
		byteStream << entry.Sequence;
		byteStream << entry.Talker;
		byteStream << textOffset;
		//Come back to check these later.
		byteStream << (byte)0;
		byteStream << (byte)0;
		byteStream << (byte)0;
		textOffset += (uint16_t)(entry.Text.length() + 1);
	}

	for (const TextEntry &entry : messageComponent.Texts)
	{
		byteStream << Win2Dos(entry.Text);
	}

	uint16_t totalSize = (uint16_t)byteStream.tellp();
	uint16_t offsetToEnd = (uint16_t)(byteStream.tellp() - startCount);
	*(reinterpret_cast<uint16_t*>(byteStream.GetInternalPointer() + fillThisIn)) = offsetToEnd;
	// NOTE: This may need to be padded to WORD boundary
}

void MessageWriteTo_4000(const TextComponent &messageComponent, sci::ostream &byteStream)
{
	uint32_t fillThisIn = byteStream.tellp();
	byteStream.WriteWord(0); // We'll fill this in later
	uint32_t startCount = byteStream.tellp();
	byteStream << messageComponent.MysteryNumber;

	UnresolveReferences((TextComponent&)messageComponent);

	byteStream.WriteWord((uint16_t)messageComponent.Texts.size());

	uint16_t textOffset = (uint16_t)(byteStream.tellp() + (5 + 2 + 4) * messageComponent.Texts.size());
	for (const TextEntry &entry : messageComponent.Texts)
	{
		byteStream << entry.Noun;
		byteStream << entry.Verb;
		byteStream << entry.Condition;
		byteStream << entry.Sequence;
		byteStream << entry.Talker;
		byteStream << textOffset;
		byteStream << entry.Reference;
		textOffset += (uint16_t)(entry.Text.length() + 1);
	}

	for (const TextEntry &entry : messageComponent.Texts)
	{
		byteStream << Win2Dos(entry.Text);
	}

	uint16_t totalSize = (uint16_t)byteStream.tellp();
	uint16_t offsetToEnd = (uint16_t)(byteStream.tellp() - startCount);
	*(reinterpret_cast<uint16_t*>(byteStream.GetInternalPointer() + fillThisIn)) = offsetToEnd;
	// NOTE: This may need to be padded to WORD boundary

	ResolveReferences((TextComponent&)messageComponent);
}

uint16_t CheckMessageVersion(sci::istream &byteStream)
{
	uint16_t msgVersion;
	byteStream >> msgVersion;
	return msgVersion;
}

void MessageReadFrom(ResourceEntity &resource, sci::istream &byteStream, const std::map<BlobKey, uint32_t> &propertyBag)
{
	TextComponent &message = resource.GetComponent<TextComponent>();

	byteStream >> message.msgVersion;
	byteStream.skip(2);
	if (message.msgVersion <= 0x835)		// 2101
	{
		message.Flags = MessagePropertyFlags::Noun | MessagePropertyFlags::Verb;
		MessageReadFrom_2102(message, byteStream);
	}
	else if (message.msgVersion <= 0xd53)   // 3411
	{
		message.Flags = MessagePropertyFlags::Noun | MessagePropertyFlags::Verb | MessagePropertyFlags::Condition | MessagePropertyFlags::Sequence | MessagePropertyFlags::Talker;
		MessageReadFrom_3411(message, byteStream);
	}
	else
	{
		message.Flags = MessagePropertyFlags::Noun | MessagePropertyFlags::Verb | MessagePropertyFlags::Condition | MessagePropertyFlags::Sequence | MessagePropertyFlags::Talker;
		MessageReadFrom_4000(message, byteStream);
	}
}

void MessageWriteTo(const ResourceEntity &resource, sci::ostream &byteStream, std::map<BlobKey, uint32_t> &propertyBag)
{
	const TextComponent &message = resource.GetComponent<TextComponent>();
	byteStream << message.msgVersion;
	byteStream.WriteWord(0);	// Unknown
	if (message.msgVersion <= 0x835)
	{
		MessageWriteTo_2102(message, byteStream);
	}
	else if (message.msgVersion <= 0xd53)
	{
		MessageWriteTo_3411(message, byteStream);
	}
	else
	{
		MessageWriteTo_4000(message, byteStream);
	}
}

std::vector<std::string> split(const std::string& value, char separator)
{
	std::vector<std::string> result;
	std::string::size_type p = 0;
	std::string::size_type q;
	while ((q = value.find(separator, p)) != std::string::npos)
	{
		result.emplace_back(value, p, q - p);
		p = q + 1;
	}
	result.emplace_back(value, p);
	return result;
}

void ExportMessageToFile(const TextComponent &message, const std::string &filename)
{
	std::ofstream file;
	file.open(filename, std::ios_base::out | std::ios_base::trunc);
	if (file.is_open())
	{
		for (const auto &entry : message.Texts)
		{
			std::string firstPart = fmt::format("{0}\t{1}\t{2}\t{3}\t{4}\t", (int)entry.Noun, (int)entry.Verb, (int)entry.Condition, (int)entry.Sequence, (int)entry.Talker);
			file << firstPart;
			// Split by line to match SV.exe's output
			std::vector<std::string> lines = split(entry.Text, '\n');
			int lineNumber = 0;
			for (const std::string &line : lines)
			{
				if (lineNumber > 0)
				{
					file << "\t\t\t\t\t"; // "Empty stuff" before next line (matches SV.exe's output)
				}
				file << line;
				file << std::endl;
				lineNumber++;
			}
		}
	}
}

void ConcatWithTabs(const std::vector<std::string> &pieces, size_t pos, std::string &text)
{
	while (pos < pieces.size())
	{
		text += "\t";
		text += pieces[pos];
		pos++;
	}
}

void ImportMessageFromFile(TextComponent &message, const std::string &filename)
{
	std::ifstream file;
	file.open(filename, std::ios_base::in);
	if (file.is_open())
	{
		std::string line;
		while (std::getline(file, line))
		{
			std::vector<std::string> linePieces = split(line, '\t');
			if (linePieces.size() >= 6)
			{
				// If the first 5 are empty, then it's an extension of the previous line
				bool empty = true;
				for (int i = 0; empty && (i < 5); i++)
				{
					empty = linePieces[i].empty();
				}
				if (!empty)
				{
					TextEntry entry = {};
					entry.Noun = (uint8_t)stoi(linePieces[0]);
					entry.Verb = (uint8_t)stoi(linePieces[1]);
					entry.Condition = (uint8_t)stoi(linePieces[2]);
					entry.Sequence = (uint8_t)stoi(linePieces[3]);
					entry.Talker = (uint8_t)stoi(linePieces[4]);
					entry.Text = linePieces[5];
					ConcatWithTabs(linePieces, 6, entry.Text);
					message.Texts.push_back(entry);
				}
				else if (!message.Texts.empty())
				{
					// Append to previous
					message.Texts.back().Text += "\n";
					message.Texts.back().Text += linePieces[5];
					ConcatWithTabs(linePieces, 6, message.Texts.back().Text);
				}
			}
		}
	}
}

bool ValidateMessage(const ResourceEntity &resource)
{
	// Check for duplicate tuples
	const TextComponent &text = resource.GetComponent<TextComponent>();
	std::unordered_map<uint32_t, const TextEntry *> tuples;
	for (const auto &entry : text.Texts)
	{
		uint32_t tuple = GetMessageTuple(entry);
		if (tuples.find(tuple) != tuples.end())
		{
			std::string message = fmt::format("Entries must be distinct. The following entries have the same noun/verb/condition/sequence:\n{0}\n{1}",
				entry.Text,
				tuples[tuple]->Text
				);

			AfxMessageBox(message.c_str(), MB_OK | MB_ICONWARNING);
			return false;
		}
		else
		{
			tuples[tuple] = &entry;
		}
	}

	// Check for sequences beginning with something other than 1.
	std::map<uint32_t, bool> tuplesHaveSeq;
	for (auto &entry : text.Texts)
	{
		uint32_t tuple = entry.Noun + (entry.Verb << 8) + (entry.Condition << 16);
		tuplesHaveSeq[tuple] = tuplesHaveSeq[tuple] || (entry.Sequence == 1);
	}
	for (auto &pair : tuplesHaveSeq)
	{
		if (!pair.second)
		{
			std::string message = fmt::format("The following mesaage: (noun:{0}, verb:{1}, cond:{2}) has a sequence that doesn't begin at 1. Save anyway?", (pair.first & 0xff), (pair.first >> 8) & 0xff, (pair.first >> 16) & 0xff);
			if (IDNO == AfxMessageBox(message.c_str(), MB_YESNO | MB_ICONWARNING))
			{
				return false;
			}
			break;
		}
	}

	return true;
}

void MessageWriteNounsAndCases(const ResourceEntity &resource, int resourceNumber)
{
	NounsAndCasesComponent *nounsAndCases = const_cast<NounsAndCasesComponent*>(resource.TryGetComponent<NounsAndCasesComponent>());
	if (nounsAndCases)
	{
		// Use the provided resource number instead of that in the ResourceEntity, since it may
		// be -1
		nounsAndCases->Commit(resourceNumber);
	}
}


ResourceTraits messageTraits =
{
	ResourceType::Message,
	&MessageReadFrom,
	&MessageWriteTo,
	&ValidateMessage,
	&MessageWriteNounsAndCases,
};

ResourceEntity *CreateMessageResource(SCIVersion version)
{
	std::unique_ptr<ResourceEntity> pResource = std::make_unique<ResourceEntity>(messageTraits);
	pResource->AddComponent(std::move(std::make_unique<TextComponent>()));
	switch (version.MessageMapSource)
	{
		case MessageMapSource::Included:
			pResource->SourceFlags = ResourceSourceFlags::ResourceMap;
			break;
		case MessageMapSource::MessageMap:
			pResource->SourceFlags = ResourceSourceFlags::MessageMap;
			break;
		case MessageMapSource::AltResMap:
			pResource->SourceFlags = ResourceSourceFlags::AltMap;
			break;
		default:
			assert(false);
			break;
	}
	return pResource.release();
}

ResourceEntity *CreateDefaultMessageResource(SCIVersion version)
{
	// Nothing different.
	return CreateMessageResource(version);
}

ResourceEntity *CreateNewMessageResource(SCIVersion version, uint16_t msgVersion)
{
	ResourceEntity *resource = CreateMessageResource(version);
	TextComponent &text = resource->GetComponent<TextComponent>();
	text.msgVersion = msgVersion;
	text.Flags = MessagePropertyFlags::Noun | MessagePropertyFlags::Verb;
	if (msgVersion > 0x835)
	{
		text.Flags |= MessagePropertyFlags::Condition | MessagePropertyFlags::Sequence | MessagePropertyFlags::Talker;
	}
	return resource;
}
