module Tests exposing (..)

import Expect
import Test exposing (Test, test)
import Morphir.Example.App.ScumAndVillainy exposing (Outcome(..), outcomeOfRolls)

testExpectBadOutcome : Test
testExpectBadOutcome =
    test "Expect bad outcome test" <|
        \() -> outcomeOfRolls [1, 2, 3] == Bad
        |> Expect.true "Expected bad"
