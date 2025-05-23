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

class ExtTabControl : public CTabCtrl
{
protected:
	DECLARE_MESSAGE_MAP()

private:
	void DrawItem(LPDRAWITEMSTRUCT);
	void DrawItemBorder(LPDRAWITEMSTRUCT);
	void DrawMainBorder(LPDRAWITEMSTRUCT);

	void OnPaint();
	BOOL OnEraseBkgnd(CDC *pDC);
};
