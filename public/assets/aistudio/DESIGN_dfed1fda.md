---
name: Serene Rupee
colors:
  surface: '#f6fafd'
  surface-dim: '#d6dbde'
  surface-bright: '#f6fafd'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f0f4f7'
  surface-container: '#eaeef2'
  surface-container-high: '#e4e9ec'
  surface-container-highest: '#dfe3e6'
  on-surface: '#171c1f'
  on-surface-variant: '#3e4947'
  inverse-surface: '#2c3134'
  inverse-on-surface: '#edf1f5'
  outline: '#6e7977'
  outline-variant: '#bec9c6'
  surface-tint: '#016a60'
  primary: '#005048'
  on-primary: '#ffffff'
  primary-container: '#006a60'
  on-primary-container: '#95e7da'
  inverse-primary: '#84d5c8'
  secondary: '#076d2e'
  on-secondary: '#ffffff'
  secondary-container: '#9cf7a7'
  on-secondary-container: '#147434'
  tertiary: '#920009'
  on-tertiary: '#ffffff'
  tertiary-container: '#b91919'
  on-tertiary-container: '#ffcbc5'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#9ff2e4'
  primary-fixed-dim: '#84d5c8'
  on-primary-fixed: '#00201c'
  on-primary-fixed-variant: '#005048'
  secondary-fixed: '#9cf7a7'
  secondary-fixed-dim: '#81da8d'
  on-secondary-fixed: '#002109'
  on-secondary-fixed-variant: '#005320'
  tertiary-fixed: '#ffdad5'
  tertiary-fixed-dim: '#ffb4ab'
  on-tertiary-fixed: '#410002'
  on-tertiary-fixed-variant: '#930009'
  background: '#f6fafd'
  on-background: '#171c1f'
  surface-variant: '#dfe3e6'
typography:
  display-lg:
    fontFamily: Inter
    fontSize: 3.5rem
    fontWeight: '700'
    lineHeight: 4rem
    letterSpacing: -0.03em
  display-md:
    fontFamily: Inter
    fontSize: 2.75rem
    fontWeight: '600'
    lineHeight: 3.25rem
    letterSpacing: -0.025em
  headline-lg:
    fontFamily: Inter
    fontSize: 2rem
    fontWeight: '600'
    lineHeight: 2.5rem
    letterSpacing: -0.02em
  headline-lg-mobile:
    fontFamily: Inter
    fontSize: 1.75rem
    fontWeight: '600'
    lineHeight: 2.25rem
    letterSpacing: -0.02em
  headline-md:
    fontFamily: Inter
    fontSize: 1.5rem
    fontWeight: '600'
    lineHeight: 2rem
    letterSpacing: -0.015em
  headline-sm:
    fontFamily: Inter
    fontSize: 1.25rem
    fontWeight: '600'
    lineHeight: 1.75rem
    letterSpacing: -0.01em
  title-lg:
    fontFamily: Inter
    fontSize: 1.125rem
    fontWeight: '600'
    lineHeight: 1.625rem
    letterSpacing: '0'
  title-md:
    fontFamily: Inter
    fontSize: 1rem
    fontWeight: '500'
    lineHeight: 1.5rem
    letterSpacing: 0.01em
  body-lg:
    fontFamily: Inter
    fontSize: 1rem
    fontWeight: '400'
    lineHeight: 1.5rem
    letterSpacing: 0.01em
  body-md:
    fontFamily: Inter
    fontSize: 0.875rem
    fontWeight: '400'
    lineHeight: 1.375rem
    letterSpacing: 0.015em
  body-sm:
    fontFamily: Inter
    fontSize: 0.75rem
    fontWeight: '400'
    lineHeight: 1.125rem
    letterSpacing: 0.02em
  label-lg:
    fontFamily: Inter
    fontSize: 0.875rem
    fontWeight: '600'
    lineHeight: 1.25rem
    letterSpacing: 0.01em
  label-md:
    fontFamily: Inter
    fontSize: 0.75rem
    fontWeight: '500'
    lineHeight: 1rem
    letterSpacing: 0.03em
  label-sm:
    fontFamily: Inter
    fontSize: 0.6875rem
    fontWeight: '600'
    lineHeight: 0.875rem
    letterSpacing: 0.04em
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  gutter: 1rem
  margin: 1rem
  space-xs: 0.25rem
  space-sm: 0.5rem
  space-md: 1rem
  space-lg: 1.25rem
  space-xl: 1.5rem
---

## Brand & Style

This design system establishes a focused, calm, and dependable financial tracking environment tailored for mobile users in Sri Lanka. It combines Google's Material Design 3 guidelines with refined, high-clarity fintech conventions. 

The aesthetic is Modern Tactile Minimalist: clean architectural layouts, deliberate negative space, and tonal containment that eschews harsh drop shadows in favor of tinted surface tiers and hairline divider borders. The emotional goal is to alleviate money-related anxiety by replacing chaotic dashboards with orderly, balanced typography, soft natural greens, and unambiguous financial signals. Every interactive element respects Android accessibility standards with minimum 48x48dp touch boundaries, ensuring fluid, tactile one-handed thumb interaction.

## Colors

The color architecture directly follows the Material Design 3 dynamic tonal palette structure, grounded in Sri Lankan botanical teals and disciplined ledger accents:

- **Primary (`#006A60`)**: Deep coastal emerald-teal anchoring primary actions, active navigation items, top app bars, and high-emphasis floating action buttons.
- **On-Primary (`#FFFFFF`)**: Pure white providing strict AAA contrast over primary surfaces.
- **Primary Container (`#A2F2E4`)**: Soft aqua-mint for low-friction tonal buttons, category badges, and highlighted selections.
- **Income Accent (`#137333`)**: Vibrant yet organic deep green designated exclusively for inflows, positive balance deltas, and completed budget milestones.
- **Expense / Alert Accent (`#BA1A1A`)**: Balanced crimson for outflows, budget breaches, overdue bills, and destructive modals.
- **Neutral / Canvas**:
  - `background`: `#FBFDFD` (crisp, faintly tinted background canvas).
  - `surface`: `#FFFFFF` (clean flat card and modal ground).
  - `surface-container`: `#F0F4F4` (tonal cards and categorized modules).
  - `outline-variant`: `#E1E9E7` (whisper-thin hairline boundaries).
- **Text & Content**:
  - Headings and figures utilize `#191C1C` for absolute optical weight.
  - Secondary metadata, timestamps, and input labels utilize `#5E6366`.

## Typography

The typographic hierarchy utilizes **Inter** across all UI layers. Financial legibility requires precise vertical number alignment; therefore, all monetary presentations, data tables, and date formats must render with open-type tabular lining figures (`font-feature-settings: "tnum" 1, "cv05" 1`).

All localized currency expressions conform strictly to the format: `LKR 12,500.00`. The currency code `LKR` is rendered in `label-md` or `title-md` weight slightly muted relative to the integer amount, while the fractional decimals remain optically stable without jumping across dynamically updated balances.

## Layout & Spacing

Layouts adhere to an 8dp architectural grid with a 4dp micro-step for compact icon and tag paddings.

- **Mobile Viewports (<600dp)**: Standard 4-column fluid layout with `16dp` (`1rem`) outer canvas margins and `16dp` gutters. Essential touch elements retain 48dp minimum clearance. Bottom navigation is anchored, leaving a dynamic bottom safe-area offset of `80dp` to avoid floating action button overlap.
- **Tablet / Large Handhelds (600dp–840dp)**: 8-column layout with `24dp` margins and `16dp` gutters.
- **Desktop / Foldable Expanded (>840dp)**: 12-column layout conforming to an inner maximum content width of `1040dp`, centered with auto margins.

## Elevation & Depth

This system departs from dark skeuomorphic drops in favor of Material Design 3 **Tonal Surface Elevation** combined with crisp structural hairline outlines. Depth is expressed as follows:

- **Level 0 (Base Canvas)**: Surface background `#FBFDFD`, completely flat with zero shadow.
- **Level 1 (Card & Section Containers)**: Surface fill `#F0F4F4` bordered by a 1px solid `#E1E9E7` outline. Zero blur; physical layer differentiation is achieved purely through chromatic value.
- **Level 2 (Active Cards / Segmented Controls)**: Surface fill `#FFFFFF` with a 1px stroke of `#E1E9E7` paired with an ultra-soft tinted ambient veil: `box-shadow: 0px 4px 12px rgba(0, 77, 64, 0.04)`.
- **Level 3 (Bottom Sheets & Modals)**: Pure `#FFFFFF` surface lifted with ambient atmospheric diffusion: `box-shadow: 0px -8px 24px rgba(25, 28, 28, 0.08)`.
- **Level 4 (Floating Action Buttons)**: Tonal teal surface `#006A60` with directional elevation: `box-shadow: 0px 6px 16px rgba(0, 106, 96, 0.28)`.

## Shapes

The design system standardizes corner geometries to reflect soft, organic precision:

- **Standard Containers & Cards**: Set to `16dp` (`rounded-lg`), producing friendly, accessible surfaces that guide the eye into grouped financial data.
- **Dialogs & Bottom Sheets**: Top corners rounded to `24dp` (`rounded-xl`) for tactile gesture cues.
- **Pills & Interactive Action Controls**: Filter chips, status badges, tag triggers, and the primary transaction Floating Action Button (FAB) use fully rounded circular capsules (`9999px`).
- **Input Fields**: Default to `12dp` internal radii, reinforcing a soft tactile container without appearing circular.

## Components

### Buttons
- **Primary FAB / Action Button**: Pill-shaped (`rounded-full`), height `56dp`, background `#006A60`, text/icon `#FFFFFF`. Elevation Level 4. Includes a minimum padding of `24dp` on horizontal axes for text-extended variations.
- **Secondary / Tonal Button**: Height `44dp`, background `#A2F2E4` with text `#004D40`, shape `rounded-full`.
- **Outlined Button**: Height `44dp`, 1px solid border `#E1E9E7`, transparent background, text `#006A60`.

### Cards & Ledger Tiles
- **Account Summary Card**: Background `#F0F4F4`, border 1px solid `#E1E9E7`, radius `16dp`, padding `20dp`. Balance rendered in `display-md` with `tnum` tabular lining.
- **Transaction List Item**: Background `#FFFFFF`, min-height `72dp`, horizontal padding `16dp`. Left slot houses a `40dp` circular category container (`#F0F4F4`), center slot provides title (`body-lg`) and timestamp (`body-sm`), right slot presents the aligned amount formatted as `+ LKR 4,500.00` in `#137333` or `- LKR 1,250.00` in `#BA1A1A`.

### Input Fields
- Structured as Material 3 filled containers with subtle bottom baseline indicators. Surface `#F0F4F4`, corner radius `12dp` on top or uniform `12dp` perimeter. Active focus shifts the border stroke to `2px` solid `#006A60`. Supporting labels float gracefully into `label-sm`. Currency inputs feature fixed `LKR` prefix adornments in `#5E6366`.

### Filter Chips & Selectors
- Height `36dp`, full pill geometry (`rounded-full`), 1px outline `#E1E9E7`. Unselected state uses `#FFFFFF` fill with `#454746` text. Selected state transforms to `#A2F2E4` container with `#004D40` text and a leading checkmark icon.

### Progress & Budget Trackers
- Track height `8dp`, rounded to `9999px`. Inactive track fill `#E1E9E7`. Active progress bar utilizes `#006A60`, dynamically transitioning to `#BA1A1A` when expenditure surpasses 90% of the assigned budget ceiling.