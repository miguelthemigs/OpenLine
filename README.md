# OpenLine

Welcome to **OpenLine** – your go-to Android app for sparking conversations, sharing opinions and diving into threaded discussions. Post an opinion, cast your “Agree” or “Disagree” votes, leave comments, and reply to others. All on a modern Jetpack Compose UI backed by a Supabase (PostgreSQL) REST API.

---

## Table of Contents

- [Overview](#overview)  
- [Features](#features)  
- [How to Use](#how-to-use)  
- [Tech Stack](#tech-stack)  
- [Contributing](#contributing)  

---

## Overview

In **OpenLine**, each “clothing item” has an associated opinion thread. Users can:

1. **Read** a community opinion  
2. **Agree/Disagree** with live vote tallies  
3. **Comment** on that opinion  
4. **Reply** to other comments in a threaded view  
5. **Preview** up to two replies inline or dive into a dedicated replies screen  

Everything syncs in real-time via a Supabase-powered REST API so you’re always seeing the latest counts and comments.

---

## Features

- **Opinion Detail Card**  
  – Text, author & timestamp  
  – “Agree” / “Disagree” buttons with live counts  

- **Tabbed Comment Sorting**  
  – Toggle between “Top” (most likes) and “Newest”  

- **Threaded Comments & Replies**  
  – Post comments under any opinion  
  – Inline preview of up to 2 replies  
  – “See more…” link opens the full replies screen  

- **Reaction System**  
  – Like/dislike each comment with live updates  
  – Agree/disagree an opinion seamlessly  

- **Reply Composer**  
  – Expand a text field to type & post replies without leaving the screen  

- **Responsive Compose UI**  
  – Scrollable lists with smooth performance  
  – Material3 theming 

---

## How to Use

1. **Launch** the app—browse or select an item’s opinion.  
2. **React** with “Agree” or “Disagree” to share your stance.  
3. **Scroll** through comments; use the tabs to sort by Top or Newest.  
4. **Reply** by tapping the “Reply” button beneath any comment.  
5. **Preview** replies inline by tapping the “›” arrow; tap again to collapse.  
6. **See more** replies or post your own in the full replies screen.  

---

## Tech Stack

- **Kotlin** & **Coroutines**  
  Modern language and asynchronous handling for network calls.

- **Jetpack Compose**  
  Declarative, performant UI toolkit for all screens and components.

- **Android ViewModel & Lifecycle**  
  Lifecycle-aware state management ensures data survives rotations.

- **Material3 Components**  
  For consistent, modern Android design and theming.

- **Supabase (PostgreSQL)**  
  Backend database + REST API for opinions, comments, replies, and reactions.

---

**Enjoy discussing?** Pull up and drop your line on **OpenLine**!  
