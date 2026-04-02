{
  description = "khs nix flake";

  inputs = {
    nixpkgs.url = "github:nixos/nixpkgs/nixos-unstable";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs = {
    nixpkgs,
    flake-utils,
    ...
  }: let
    supportedSystems = let
      inherit (flake-utils.lib) system;
    in [
      system.aarch64-linux
      system.aarch64-darwin
      system.x86_64-linux
    ];
  in
    flake-utils.lib.eachSystem supportedSystems (system: let
      pkgs = import nixpkgs {
        inherit system;
        config.allowUnfree = true;
      };
    in {
      devShell = pkgs.mkShell {
        packages = with pkgs; [
          (gradle_9.override {
            javaToolchains = [
              openjdk8
              openjdk17
              openjdk21
            ];
          })
          kotlin-language-server
        ];

        shellHook = ''
          export JAVA_HOME="${pkgs.openjdk21}/lib/openjdk"

          ktfmt() {
            find . -name "*.(kt|kts)" | xargs ${pkgs.ktfmt}/bin/ktfmt --kotlinlang-style "$@"
          }
        '';

      };

      formatter = pkgs.alejandra;
    });
}
