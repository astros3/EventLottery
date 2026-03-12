# Firestore Database Structure

This document describes the Firebase Firestore database structure **as used by the current codebase**. It reflects only what is already implemented (read/write operations in the app).

---

## Overview

The app uses **three top-level collections**:

| Collection    | Purpose |
|---------------|--------|
| `events`      | Event definitions and per-event waiting list. |
| `users`       | **Entrant** account profile data (name, email, phone). Keyed by device ID. |
| `organizers`  | **Organizer** account profile (firstName, lastName, email, phoneNumber). Keyed by organizer ID. |

**Entrants and organizers are separate account types** (see CRC: Account/Entrant/Organizer). One device can have both: e.g. `users/{deviceId}` (entrant profile) and `organizers/{deviceId}` (organizer account). Only organizer accounts can create or edit events; the event’s `organizerId` refers to a document in `organizers`.

### Roles (consistent across the app)

| Role        | Purpose | Storage | Who can do what |
|-------------|--------|--------|------------------|
| **Entrant** | People **entering** events (view details, join/leave waiting list, profile). | `users/{deviceId}` | Join/leave waiting list; view event details; manage own profile. |
| **Organizer** | People **creating and editing** events (create event, set registration, QR code, etc.). | `organizers/{organizerId}` | Create events (only if they have an organizer account); **only the organizer who created an event can edit that event** and view/edit its QR code. |

All code that creates or edits events, or that views/edits an event’s QR code, checks that the current user is the event’s organizer (`event.getOrganizerId()` equals the current device/organizer ID). Entrant-only flows use `users` and waiting list subcollections only.

All paths and field names below are taken from the Java model classes and the Firestore calls in the app.

---

## 1. `events` collection

**Path:** `events/{eventId}`

Each document represents one event. The document ID is the **event ID** (e.g. a Firestore auto-generated ID when creating a new event).

### 1.1 Document ID

- **Field in code:** used as `eventId` (also stored as a field on the document).
- **How it’s created:** `db.collection("events").document().getId()` when creating a new event in `EventEditActivity`.
- **Usage:** Used everywhere the app loads or updates an event (event details, edit, QR screen, join/leave waiting list).

### 1.2 Event document fields

These fields come from the `Event` Java class. Firestore maps them by the JavaBean-style names (getters/setters), so the stored field names are **camelCase** as below.

| Field name (in Firestore) | Type    | Description |
|---------------------------|---------|-------------|
| `eventId`                 | string  | Same as the document ID; set when saving the event. |
| `title`                   | string  | Event name. |
| `description`             | string  | Event description. |
| `location`                | string  | Event location. |
| `organizerId`             | string  | Identifier of the organizer (currently device ID). |
| `organizerName`           | string  | Display name of the organizer (e.g. "Organizer1"). |
| `capacity`                | number  | Max number of accepted entrants (0 = unlimited). Not yet used in create/edit UI; can be 0. |
| `waitingListLimit`        | number  | Max number of entrants on the waiting list (0 = unlimited). Set in event create/edit. |
| `registrationStartMillis` | number  | Registration window start time (epoch milliseconds). |
| `registrationEndMillis`   | number  | Registration window end time (epoch milliseconds). |
| `eventDateMillis`         | number  | Event date/time (epoch milliseconds). Set from registration end in current create flow. |
| `geolocationRequired`     | boolean | Whether entrants must provide geolocation when joining. |
| `posterUri`               | string  | URI/URL of event poster image. Optional; upload not implemented yet. |
| `qrCodeUri`               | string  | URI for event’s QR code image. Optional; QR is generated on device, not stored here. |
| `promoCode`               | string  | Human-readable code (e.g. "555 555") for manual entry. Set on create; can be updated from QR screen. |
| `price`                   | number  | Price to attend (e.g. 0.0 for free). |
| `selectionCriteria`       | array   | List of strings describing lottery/selection criteria. Can be empty. |

**Notes:**

- All of the above can be written when creating or updating an event in `EventEditActivity` (except `promoCode`, which is also updated from `QRCodeActivity` via `update("promoCode", code)`).
- When loading an event, the app uses `document.getData()` / `toObject(Event.class)`, so any field present in the document is mapped into `Event` if the field name and type match.

---

## 2. `events/{eventId}/waitingList` subcollection

**Path:** `events/{eventId}/waitingList/{deviceId}`

Each document represents one entrant’s place on that event’s waiting list. The document ID is the **device ID** of the entrant (from `DeviceIdManager.getDeviceId(context)`).

### 2.1 Document ID

- **Value:** `deviceId` (same value as in the `deviceId` field).
- **Usage:** One document per device per event. Used to check “am I on the list?”, join (create/overwrite), and leave (delete).

### 2.2 Waiting list document fields

From the `WaitingListEntry` class:

| Field name (in Firestore) | Type     | Description |
|---------------------------|----------|-------------|
| `deviceId`                | string   | Same as the document ID; identifies the entrant. |
| `status`                  | string   | One of: `WAITING`, `PENDING`, `SELECTED`, `ACCEPTED`, `DECLINED`, `CANCELLED`. Currently the app only writes `PENDING` when joining. |
| `joinTimestamp`           | timestamp| Firestore `Timestamp` when the entrant joined (set in `WaitingListEntry` constructor with `Timestamp.now()`). |

**Operations in code:**

- **Read:** `events/{eventId}/waitingList/{deviceId}.get()` to see if the current user is on the list (e.g. in `EventDetailsActivity`).
- **Join:** `events/{eventId}/waitingList/{deviceId}.set(entry)` with a new `WaitingListEntry(deviceId, Status.PENDING)`.
- **Leave:** `events/{eventId}/waitingList/{deviceId}.delete()`.

There are no other subcollections under `events` in the current code.

---

## 3. `organizers` collection

**Path:** `organizers/{organizerId}`

Each document represents one **organizer account** (CRC: Organizer). This is separate from entrant accounts (`users`). The same device can have both an entrant profile (`users/{deviceId}`) and an organizer account (`organizers/{organizerId}`); when the app uses `deviceId` as `organizerId`, that device is acting as an organizer.

Only users with an organizer account can create events. When creating an event, the app checks that `organizers/{organizerId}` exists (e.g. `organizerId == deviceId`); if not, it can offer to “Register as organizer,” which creates this document.

### 3.1 Document ID

- **Value:** `organizerId` (in the current code, the device ID when the device is registered as an organizer).
- **Usage:** One organizer account per ID. Used in `EventEditActivity` to verify organizer before create and to read `displayName` for `event.organizerName`.

### 3.2 Organizer document fields

Organizers are a separate account type from users (entrants), with their own profile fields. From the `Organizer` class:

| Field name (in Firestore) | Type   | Description |
|---------------------------|--------|--------------|
| `organizerId`             | string | Same as the document ID. |
| `firstName`               | string | Organizer’s first name. |
| `lastName`                | string | Organizer’s last name. |
| `email`                   | string | Organizer’s email. |
| `phoneNumber`             | string | Organizer’s phone (optional). |
| `displayName`             | string | Optional display name; used as fallback if first/last name are empty. |

**Operations in code:**

- **Read:** `organizers/{deviceId}.get()` in `EventEditActivity.ensureOrganizerAccountThenAllowCreate()` to allow create and to get display name.
- **Write:** `organizers/{deviceId}.set(organizer)` when the user registers as organizer (e.g. from the “Only organizer accounts can create events” dialog).

There are no subcollections under `organizers`.

---

## 4. `users` collection

**Path:** `users/{deviceId}`

Each document holds the profile for one **entrant** (entrant account only; not used for organizer identity). The document ID is the **device ID** (from `DeviceIdManager.getDeviceId(context)`).

### 4.1 Document ID

- **Value:** `deviceId`.
- **Usage:** One entrant profile per device. Used in `EntrantProfileActivity` to load and save the profile.

### 4.2 User document fields

From the `Entrant` class:

| Field name (in Firestore) | Type   | Description |
|---------------------------|--------|-------------|
| `firstName`               | string | Entrant’s first name. |
| `lastName`                | string | Entrant’s last name. |
| `email`                   | string | Entrant’s email. |
| `phoneNumber`             | string | Optional phone number. |

**Operations in code:**

- **Read:** `users/{deviceId}.get()` in `EntrantProfileActivity.loadProfile()`, then `toObject(Entrant.class)`.
- **Write:** `users/{deviceId}.set(entrant)` in `EntrantProfileActivity.updateProfile()` (full document overwrite).

There are no subcollections under `users` in the current code.

---

## 5. Visual summary

```
Firestore root
│
├── events (collection)
│   └── {eventId} (document)
│       ├── eventId, title, description, location, organizerId, organizerName,
│       ├── capacity, waitingListLimit, registrationStartMillis, registrationEndMillis,
│       ├── eventDateMillis, geolocationRequired, posterUri, qrCodeUri,
│       ├── promoCode, price, selectionCriteria
│       │
│       └── waitingList (subcollection)
│           └── {deviceId} (document)
│               ├── deviceId
│               ├── status
│               └── joinTimestamp
│
├── organizers (collection)   ← organizer accounts (separate from entrants; own profile)
│   └── {organizerId} (document)
│       ├── organizerId
│       ├── firstName, lastName, email, phoneNumber
│       └── displayName (optional)
│
└── users (collection)        ← entrant accounts only
    └── {deviceId} (document)
        ├── firstName
        ├── lastName
        ├── email
        └── phoneNumber
```

---

## 6. Where each path is used in code

| Path / operation | File | Purpose |
|------------------|------|--------|
| `events` doc read | `EventEditActivity` | Load event for edit; verify current user is organizer. |
| `events` doc read | `EventDetailsActivity` | Load event for display and to check registration open. |
| `events` doc read | `QRCodeActivity` | Load event to show/update promo code. |
| `events` doc write (set) | `EventEditActivity` | Create new event or save full event on edit. |
| `events` doc write (update) | `QRCodeActivity` | Update only `promoCode`. |
| `events/{eventId}/waitingList/{deviceId}` read | `EventDetailsActivity` | Check if current user is on waiting list. |
| `events/{eventId}/waitingList/{deviceId}` set | `EventDetailsActivity` | Join waiting list. |
| `events/{eventId}/waitingList/{deviceId}` delete | `EventDetailsActivity` | Leave waiting list. |
| `organizers/{organizerId}` read | `EventEditActivity` | Verify organizer account before create; get display name. |
| `organizers/{organizerId}` set | `EventEditActivity` | Register device as organizer. |
| `users/{deviceId}` read | `EntrantProfileActivity` | Load entrant profile. |
| `users/{deviceId}` set | `EntrantProfileActivity` | Save entrant profile. |

---

## 7. Data types and conventions

- **Timestamps:** `WaitingListEntry.joinTimestamp` is a Firestore `Timestamp` (from `com.google.firebase.Timestamp`). Event dates are stored as **numbers** (long milliseconds since epoch), not Firestore Timestamp.
- **Lists:** `Event.selectionCriteria` is stored as a Firestore **array** (list of strings).
- **Document IDs:** Event IDs and device IDs are **opaque strings**; the app does not assume any format beyond that they are unique and used consistently as document IDs and, where applicable, as fields (`eventId`, `deviceId`).

This document does not describe any planned or future schema; it only reflects the database shape implied by the code that exists today.
