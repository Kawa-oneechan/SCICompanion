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

#include "PaletteOperations.h"
#include "PaletteEditorCommon.h"

class CPaletteDoc;

class CPaletteView : public PaletteEditorCommon< CExtWA < CExtWS < CExtAFV < CFormView >  > > >
{
	DECLARE_DYNCREATE(CPaletteView)

protected:
	CPaletteView();		   // protected constructor used by dynamic creation
	virtual ~CPaletteView();

public:
	CPaletteDoc* GetDocument() const;

public:
#ifdef _DEBUG
	virtual void AssertValid() const;
	virtual void Dump(CDumpContext& dc) const;
#endif
	void OnUpdate(CView *pSender, LPARAM lHint, CObject *pHint) override;
	void OnInitialUpdate() override;

};



#ifndef _DEBUG  // debug version in VocabView.cpp
inline CPaletteDoc* CPaletteView::GetDocument() const
{ return reinterpret_cast<CPaletteDoc*>(m_pDocument); }
#endif
