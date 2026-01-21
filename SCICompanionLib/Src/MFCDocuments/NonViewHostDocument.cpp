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
// NonViewHostDocument.cpp : implementation file
//

#include "stdafx.h"
#include "AppState.h"
#include "NonViewHostDocument.h"


// CNonViewHostDocument
void CNonViewHostDocument::UpdateAllViewsAndNonViews(CView *pSender, LPARAM lHint, CObject *pObject)
{
	for (auto& nonViewClient : _nonViewClients)
	{
		nonViewClient->UpdateNonView(pObject);
	}

	__super::UpdateAllViews(pSender, lHint, pObject);
}

void CNonViewHostDocument::AddNonViewClient(INonViewClient *pClient)
{
	_nonViewClients.insert(pClient);
}
