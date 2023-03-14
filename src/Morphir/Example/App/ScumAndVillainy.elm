module Morphir.Example.App.ScumAndVillainy exposing (..)

import List exposing (filter, length, maximum)
import Maybe exposing (withDefault)

type SuccessLevel = Critical | Full | Partial
type Outcome = Success SuccessLevel | Bad
type Position = Controlled | Risky | Desperate
type EffectLevel = Zero | Limited | Standard | Great | Extreme
type alias Rolls = List Int

type alias DiceService = { roll : Int -> Rolls}

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
        Critical -> Great
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
