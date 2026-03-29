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
      pkgs = import nixpkgs {inherit system;};
    in {
      devShell = pkgs.mkShell {
        packages = with pkgs; [
          (gradle.override {
            javaToolchains = [
              openjdk8
              openjdk17
            ];
          })
          kotlin
          kotlin-language-server
        ];

        shellHook = ''
          ktfmt() {
            find . -name "*.kt" | xargs ${pkgs.ktfmt}/bin/ktfmt --kotlinlang-style "$@"
          }
        '';
      };

      formatter = pkgs.alejandra;
    });
}
