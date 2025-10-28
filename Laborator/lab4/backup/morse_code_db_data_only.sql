--
-- PostgreSQL database dump
--

\restrict oKr6ga2rsIhRMvpHdsoWIAZa6wglCL4wdsv9vtQY6S6ZrB5dNgljdA0sO0j0UYx

-- Dumped from database version 15.14 (Homebrew)
-- Dumped by pg_dump version 15.14 (Homebrew)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Data for Name: achievements; Type: TABLE DATA; Schema: public; Owner: morsemate
--

COPY public.achievements (id, created_at, updated_at, criteria, description, gem_reward, icon, name, points) FROM stdin;
\.


--
-- Data for Name: categories; Type: TABLE DATA; Schema: public; Owner: morsemate
--

COPY public.categories (id, created_at, updated_at, description, display_order, icon_url, is_active, name) FROM stdin;
\.


--
-- Data for Name: lessons; Type: TABLE DATA; Schema: public; Owner: morsemate
--

COPY public.lessons (id, created_at, updated_at, content, description, difficulty, estimated_duration, order_index, points_reward, title, category_id) FROM stdin;
\.


--
-- Data for Name: exercises; Type: TABLE DATA; Schema: public; Owner: morsemate
--

COPY public.exercises (id, created_at, updated_at, correct_answer, difficulty, hint, options, points, question, time_limit, type, lesson_id) FROM stdin;
\.


--
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: morsemate
--

COPY public.users (id, created_at, updated_at, current_streak, email, email_verified, full_name, hearts, is_active, last_heart_refill, last_login, level, longest_streak, max_hearts, password_hash, profile_picture_url, total_points, username, role) FROM stdin;
2	2025-10-20 16:13:36.552633	2025-10-20 16:13:36.552588	0	mertaydogn01@gmail.com	f	Mert Aydogan	5	t	2025-10-20 16:13:36.552268	\N	1	0	5	$2a$10$VShVqy7mcBzbH2NYxeUUmeUCuwh.HcTMCD6TwHk3PE0sGoGpdniJy	\N	0	aydgn1	USER
1	2025-10-20 16:07:03.506383	2025-10-20 16:07:03.505791	0	mertaydogn0@gmail.com	f	Mert Aydogan	5	t	2025-10-20 16:07:03.488234	2025-10-20 16:16:12.074001	1	0	5	$2a$10$9CVkefy/YEyShWH0KDjBcuWajqHm40Y/lNVVFHCRi4xdlt8prPwH6	\N	0	aydgn	USER
3	2025-10-20 16:22:44.754145	2025-10-20 16:22:44.754106	0	mert@gmail.com	f	Mert Aydogan	5	t	2025-10-20 16:22:44.753817	2025-10-20 17:37:15.148533	1	0	5	$2a$10$ffpsGBGgBFKi5PZq2fzF8OzhZeBPRbhrwhCOtmFPQW7GC5FI.Cm5C	\N	0	john_de	USER
12	2025-10-20 17:44:39.275139	2025-10-20 17:44:39.274674	0	john@example1.com	f	Mert Aydogan	5	t	2025-10-20 17:44:39.265796	\N	1	0	5	$2a$10$VfuUl8V0nEHqG/q.AhH0C.cuVeCwrmDYcG4cfsDAlqGgxvcVziGI.	\N	0	joh1n_doe	USER
13	2025-10-20 17:51:35.302879	2025-10-20 17:52:22.543437	0	user1760971895183@test.com	f	Test User Updated	5	t	2025-10-20 17:51:35.302537	\N	1	0	5	$2a$10$rBAd.cdbx1IXbkzqguVBOeuWUuvTGJjF4Q.o7aBds3ZkiZyUWwgDW	https://example.com/avatar.jpg	0	user_1760971895183	USER
4	2025-10-20 16:23:20.698139	2025-10-20 16:23:20.698086	0	john@example.com	f	Mert Aydogan	5	t	2025-10-20 16:23:20.697742	2025-10-22 19:14:15.538385	1	0	5	$2a$10$L32S9p1cRsZN400YTfO/q.1lo.iUAT1sf9gK5WUT07PazHwSJ7ZGC	\N	0	john_doe	USER
\.


--
-- Data for Name: exercise_attempts; Type: TABLE DATA; Schema: public; Owner: morsemate
--

COPY public.exercise_attempts (id, attempted_at, is_correct, points_earned, time_taken, user_answer, exercise_id, user_id) FROM stdin;
\.


--
-- Data for Name: gem_transactions; Type: TABLE DATA; Schema: public; Owner: morsemate
--

COPY public.gem_transactions (id, created_at, updated_at, amount, description, source, transaction_type, user_id) FROM stdin;
\.


--
-- Data for Name: subscription_plans; Type: TABLE DATA; Schema: public; Owner: morsemate
--

COPY public.subscription_plans (id, created_at, updated_at, billing_period, daily_practice_limit, description, features, is_active, max_hearts, name, price) FROM stdin;
\.


--
-- Data for Name: user_subscriptions; Type: TABLE DATA; Schema: public; Owner: morsemate
--

COPY public.user_subscriptions (id, created_at, updated_at, auto_renew, end_date, next_billing_date, start_date, status, stripe_subscription_id, plan_id, user_id) FROM stdin;
\.


--
-- Data for Name: payments; Type: TABLE DATA; Schema: public; Owner: morsemate
--

COPY public.payments (id, amount, currency, metadata, payment_method, status, stripe_payment_id, transaction_date, subscription_id, user_id) FROM stdin;
\.


--
-- Data for Name: power_ups; Type: TABLE DATA; Schema: public; Owner: morsemate
--

COPY public.power_ups (id, created_at, updated_at, cost_gems, description, duration_hours, icon, is_active, name, type) FROM stdin;
\.


--
-- Data for Name: promo_codes; Type: TABLE DATA; Schema: public; Owner: morsemate
--

COPY public.promo_codes (id, created_at, updated_at, applicable_plans, code, current_uses, discount_type, discount_value, is_active, max_uses, valid_from, valid_until) FROM stdin;
\.


--
-- Data for Name: user_achievements; Type: TABLE DATA; Schema: public; Owner: morsemate
--

COPY public.user_achievements (id, created_at, updated_at, earned_at, achievement_id, user_id) FROM stdin;
\.


--
-- Data for Name: user_gems; Type: TABLE DATA; Schema: public; Owner: morsemate
--

COPY public.user_gems (id, balance, last_updated, total_earned, total_spent, user_id) FROM stdin;
\.


--
-- Data for Name: user_power_ups; Type: TABLE DATA; Schema: public; Owner: morsemate
--

COPY public.user_power_ups (id, activated_at, expires_at, is_active, is_used, purchased_at, power_up_id, user_id) FROM stdin;
\.


--
-- Data for Name: user_progress; Type: TABLE DATA; Schema: public; Owner: morsemate
--

COPY public.user_progress (id, attempts, completed_at, is_completed, score, stars_earned, time_spent, lesson_id, user_id) FROM stdin;
\.


--
-- Name: achievements_id_seq; Type: SEQUENCE SET; Schema: public; Owner: morsemate
--

SELECT pg_catalog.setval('public.achievements_id_seq', 1, false);


--
-- Name: categories_id_seq; Type: SEQUENCE SET; Schema: public; Owner: morsemate
--

SELECT pg_catalog.setval('public.categories_id_seq', 1, false);


--
-- Name: exercise_attempts_id_seq; Type: SEQUENCE SET; Schema: public; Owner: morsemate
--

SELECT pg_catalog.setval('public.exercise_attempts_id_seq', 1, false);


--
-- Name: exercises_id_seq; Type: SEQUENCE SET; Schema: public; Owner: morsemate
--

SELECT pg_catalog.setval('public.exercises_id_seq', 1, false);


--
-- Name: gem_transactions_id_seq; Type: SEQUENCE SET; Schema: public; Owner: morsemate
--

SELECT pg_catalog.setval('public.gem_transactions_id_seq', 1, false);


--
-- Name: lessons_id_seq; Type: SEQUENCE SET; Schema: public; Owner: morsemate
--

SELECT pg_catalog.setval('public.lessons_id_seq', 1, false);


--
-- Name: payments_id_seq; Type: SEQUENCE SET; Schema: public; Owner: morsemate
--

SELECT pg_catalog.setval('public.payments_id_seq', 1, false);


--
-- Name: power_ups_id_seq; Type: SEQUENCE SET; Schema: public; Owner: morsemate
--

SELECT pg_catalog.setval('public.power_ups_id_seq', 1, false);


--
-- Name: promo_codes_id_seq; Type: SEQUENCE SET; Schema: public; Owner: morsemate
--

SELECT pg_catalog.setval('public.promo_codes_id_seq', 1, false);


--
-- Name: subscription_plans_id_seq; Type: SEQUENCE SET; Schema: public; Owner: morsemate
--

SELECT pg_catalog.setval('public.subscription_plans_id_seq', 1, false);


--
-- Name: user_achievements_id_seq; Type: SEQUENCE SET; Schema: public; Owner: morsemate
--

SELECT pg_catalog.setval('public.user_achievements_id_seq', 1, false);


--
-- Name: user_gems_id_seq; Type: SEQUENCE SET; Schema: public; Owner: morsemate
--

SELECT pg_catalog.setval('public.user_gems_id_seq', 1, false);


--
-- Name: user_power_ups_id_seq; Type: SEQUENCE SET; Schema: public; Owner: morsemate
--

SELECT pg_catalog.setval('public.user_power_ups_id_seq', 1, false);


--
-- Name: user_progress_id_seq; Type: SEQUENCE SET; Schema: public; Owner: morsemate
--

SELECT pg_catalog.setval('public.user_progress_id_seq', 1, false);


--
-- Name: user_subscriptions_id_seq; Type: SEQUENCE SET; Schema: public; Owner: morsemate
--

SELECT pg_catalog.setval('public.user_subscriptions_id_seq', 1, false);


--
-- Name: users_id_seq; Type: SEQUENCE SET; Schema: public; Owner: morsemate
--

SELECT pg_catalog.setval('public.users_id_seq', 13, true);


--
-- PostgreSQL database dump complete
--

\unrestrict oKr6ga2rsIhRMvpHdsoWIAZa6wglCL4wdsv9vtQY6S6ZrB5dNgljdA0sO0j0UYx

