module Morphir.Example.App.ScumAndVillainy exposing (..)

import List exposing (filter, length, maximum)
import Maybe exposing (withDefault)

type alias Enum a = {
    values: List a,
    fst: a,
    lst: a}
ord : Enum a -> a -> Int
ord e a =
    e.values
        |> List.indexedMap Tuple.pair
        |> filter (\(_, item) -> item == a)
        |> List.map Tuple.first
        |> List.head
        |> withDefault -1
succClip : Enum a -> a -> a
succClip e a =
    let pos = (ord e a) + 1 in
    e.values |> List.drop pos |> List.head |> withDefault e.lst

predClip : Enum a -> a -> a
predClip e a =
    let pos = (ord e a) - 1 in
    e.values |> List.drop pos |> List.head |> withDefault e.fst

type SuccessLevel = Critical | Full | Partial
successLevel : Enum SuccessLevel
successLevel = {
    values = [Partial, Full, Critical],
    fst = Partial,
    lst = Critical }

type Outcome = Success SuccessLevel | Bad
type Position = Controlled | Risky | Desperate
type EffectLevel = Zero | Limited | Standard | Great | Extreme
type alias Rolls = List Int

type alias DiceService = { roll : Int -> Rolls }

outcomeOfRolls : Rolls -> Outcome
outcomeOfRolls numbers =
    let
        maxRoll = numbers |> maximum |> withDefault 0
    in
    if maxRoll <= 3 then Bad
    else if maxRoll < 6 then Success Partial
    else let
        numberOfSixes = numbers |> filter (\n -> n == 6) |> length
    in
    if numberOfSixes > 1
    then Success Critical
    else Success Full

action : Position -> SuccessLevel -> EffectLevel
action p s = case p of
    Controlled -> case s of
        Critical -> Extreme
        Full -> Great
        Partial -> Standard
    Risky -> case s of
        Critical -> Great
        Full -> Standard
        Partial -> Limited
    Desperate -> case s of
        Critical -> Standard
        Full -> Limited
        Partial -> Limited

actionRoll : Int -> Position -> EffectLevel -> DiceService -> List (Position, EffectLevel)
actionRoll attribute startingPosition expectedEffectLevel diceService =
    let rolls = diceService.roll attribute in
    let outcome = outcomeOfRolls rolls in
    case outcome of
        Success s -> [(startingPosition, action startingPosition s)]
        Bad -> [(startingPosition, Zero)]