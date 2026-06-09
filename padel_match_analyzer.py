#!/usr/bin/env python3

import argparse
import unicodedata
from collections import defaultdict
from itertools import combinations


def parse_match(line):
    """Parse a match line like 'AB - CD'."""
    cleaned = line.strip()
    if not cleaned:
        return None

    separator = " - "
    if separator in cleaned:
        left, right = cleaned.split(separator, 1)
        return (tuple(left.strip()), tuple(right.strip()))

    raise ValueError(
        f"Invalid match format: {line!r}. Use lines like 'AB - CD'."
    )


def analyze_matches(match_lines):
    matches = []
    players = set()

    for line in match_lines:
        parsed = parse_match(line)
        if parsed is None:
            continue
        team_a, team_b = parsed
        matches.append((team_a, team_b))
        players.update(team_a)
        players.update(team_b)

    match_count = defaultdict(int)
    rest_streaks_over_1 = defaultdict(int)
    play_streaks_over_limit = defaultdict(int)
    partner_map = defaultdict(set)
    opponent_map = defaultdict(set)
    consecutive_repeated_couples = []

    max_play_streak = 4 if len(players) == 5 else 3

    for player in players:
        current_rest_streak = 0
        current_play_streak = 0

        for team_a, team_b in matches:
            if player in team_a or player in team_b:
                match_count[player] += 1
                if current_rest_streak > 1:
                    rest_streaks_over_1[player] += 1
                current_rest_streak = 0
                current_play_streak += 1
            else:
                if current_play_streak > max_play_streak:
                    play_streaks_over_limit[player] += 1
                current_play_streak = 0
                current_rest_streak += 1

        if current_rest_streak > 1:
            rest_streaks_over_1[player] += 1
        if current_play_streak > max_play_streak:
            play_streaks_over_limit[player] += 1

    matchup_map = defaultdict(set)
    all_couples = {
        tuple(sorted(couple)) for couple in combinations(sorted(players), 2)
    }
    all_possible_matchups = set()

    for team_a, team_b in matches:
        canonical_team_a = tuple(sorted(team_a))
        canonical_team_b = tuple(sorted(team_b))
        matchup = tuple(sorted((canonical_team_a, canonical_team_b)))

        partner_map[canonical_team_a[0]].add(canonical_team_a[1])
        partner_map[canonical_team_a[1]].add(canonical_team_a[0])
        partner_map[canonical_team_b[0]].add(canonical_team_b[1])
        partner_map[canonical_team_b[1]].add(canonical_team_b[0])

        matchup_map[canonical_team_a].add(canonical_team_b)
        matchup_map[canonical_team_b].add(canonical_team_a)

        for player in team_a:
            opponent_map[player].update(team_b)
        for player in team_b:
            opponent_map[player].update(team_a)

    for previous_index, (previous_team_a, previous_team_b) in enumerate(matches[:-1], start=1):
        previous_couples = {
            tuple(sorted(previous_team_a)),
            tuple(sorted(previous_team_b)),
        }
        next_team_a, next_team_b = matches[previous_index]
        next_couples = {
            tuple(sorted(next_team_a)),
            tuple(sorted(next_team_b)),
        }
        repeated = sorted(previous_couples & next_couples)
        if repeated:
            consecutive_repeated_couples.append((previous_index, previous_index + 1, repeated))

    for couple_a, couple_b in combinations(sorted(all_couples), 2):
        if set(couple_a).isdisjoint(couple_b):
            all_possible_matchups.add(tuple(sorted((couple_a, couple_b))))

    observed_matchups = {
        tuple(sorted((tuple(sorted(team_a)), tuple(sorted(team_b)))))
        for team_a, team_b in matches
    }
    missing_matchups = sorted(all_possible_matchups - observed_matchups)

    return (
        sorted(players),
        dict(match_count),
        dict(rest_streaks_over_1),
        dict(play_streaks_over_limit),
        {player: sorted(partners) for player, partners in partner_map.items()},
        {player: sorted(opponents) for player, opponents in opponent_map.items()},
        {
            "max_play_streak": max_play_streak,
            "consecutive_repeated_couples": consecutive_repeated_couples,
            "all_couple_matchups_covered": not missing_matchups,
            "missing_couple_matchups": missing_matchups,
            "observed_couple_matchups": len(observed_matchups),
            "possible_couple_matchups": len(all_possible_matchups),
            "equal_matches_played": len({match_count.get(player, 0) for player in players}) == 1,
        },
    )


def read_matches_from_file(path):
    with open(path, "r", encoding="utf-8") as file:
        return [line.rstrip("\n") for line in file]


def format_pass_fail(value):
    return "✅" if value else "❌"


def display_width(value):
    text = str(value)
    width = 0
    for char in text:
        if unicodedata.combining(char):
            continue
        if char in {"✅", "❌"}:
            width += 2
        elif unicodedata.east_asian_width(char) in {"F", "W"}:
            width += 2
        else:
            width += 1
    return width


def pad_cell(value, width):
    text = str(value)
    return text + (" " * max(0, width - display_width(text)))


def format_match_count_groups(players, match_count):
    grouped_players = defaultdict(list)
    for player in players:
        grouped_players[match_count.get(player, 0)].append(player)

    if len(grouped_players) == 1:
        return str(match_count.get(players[0], 0))

    parts = []
    for count in sorted(grouped_players):
        parts.append(f"{count} ({''.join(grouped_players[count])})")
    return ", ".join(parts)


def print_table(headers, rows):
    widths = [display_width(header) for header in headers]
    for row in rows:
        for index, cell in enumerate(row):
            widths[index] = max(widths[index], display_width(cell))

    def format_row(row):
        return " | ".join(
            pad_cell(cell, widths[index]) for index, cell in enumerate(row)
        )

    separator = "-+-".join("-" * width for width in widths)

    print(format_row(headers))
    print(separator)
    for row in rows:
        print(format_row(row))


def main():
    parser = argparse.ArgumentParser(
        description=(
            "Analyze a schedule of padel matches and count matches played per player "
            "plus per-player and couple-level constraints."
        )
    )
    parser.add_argument(
        "file",
        nargs="?",
        help=(
            "Optional text file with one match per line, for example: 'AB - CD'"
        ),
    )
    args = parser.parse_args()

    if args.file:
        match_lines = read_matches_from_file(args.file)
    else:
        match_lines = [
            "AB - CD",
            "CD - DE",
            "AB - EF",
            "CD - EF",
            "AB - DE",
        ]

    (
        players,
        match_count,
        rest_streaks_over_1,
        play_streaks_over_limit,
        partner_map,
        opponent_map,
        schedule_checks,
    ) = analyze_matches(match_lines)

    print("Schedule:")
    for index, match in enumerate(match_lines, start=1):
        if match.strip():
            print(f"{index:>2}. {match}")

    print("\nPlayer results:")
    player_rows = []
    for player in players:
        partnered_with_everyone = len(partner_map.get(player, [])) == len(players) - 1
        faced_everyone = len(opponent_map.get(player, [])) == len(players) - 1
        player_rows.append(
            [
                player,
                format_pass_fail(rest_streaks_over_1.get(player, 0) == 0),
                format_pass_fail(play_streaks_over_limit.get(player, 0) == 0),
                format_pass_fail(partnered_with_everyone),
                format_pass_fail(faced_everyone),
            ]
        )
    print_table(
        [
            "Player",
            "Rest <= 1",
            f"Play <= {schedule_checks['max_play_streak']}",
            "Partner All",
            "Face All",
        ],
        player_rows,
    )

    print("\nSchedule filters:")
    repeated_couples_ok = not schedule_checks["consecutive_repeated_couples"]
    matches_detail = format_match_count_groups(players, match_count)
    schedule_rows = [
        [
            "Same matches played",
            format_pass_fail(schedule_checks["equal_matches_played"]),
            matches_detail,
        ],
        ["No consecutive repeated couples", format_pass_fail(repeated_couples_ok), "-"],
        [
            "All couple-vs-couple matchups covered",
            format_pass_fail(schedule_checks["all_couple_matchups_covered"]),
            f"{schedule_checks['observed_couple_matchups']} / {schedule_checks['possible_couple_matchups']}",
        ],
    ]
    print_table(["Rule", "Pass", "Details"], schedule_rows)

    if schedule_checks["consecutive_repeated_couples"]:
        repeated_descriptions = []
        for first_match, second_match, couples in schedule_checks["consecutive_repeated_couples"]:
            repeated_descriptions.append(
                f"matches {first_match}-{second_match}: "
                + ", ".join("".join(couple) for couple in couples)
            )
        print("\nRepeated consecutive couples:")
        for description in repeated_descriptions:
            print(f"- {description}")

    if schedule_checks["missing_couple_matchups"]:
        missing = ", ".join(
            f"{''.join(left)} vs {''.join(right)}"
            for left, right in schedule_checks["missing_couple_matchups"]
        )
        print("\nMissing couple-vs-couple matchups:")
        print(missing)


if __name__ == "__main__":
    main()
