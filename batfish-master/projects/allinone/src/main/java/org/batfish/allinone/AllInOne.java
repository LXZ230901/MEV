package org.batfish.allinone;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import org.batfish.allinone.config.Settings;
import org.batfish.client.Client;
import org.batfish.client.Command;
import org.batfish.common.BatfishLogger;
import org.batfish.common.util.BindPortFutures;
import org.batfish.coordinator.BatfishWorkerServiceWorkExecutor;
import org.batfish.coordinator.WorkExecutorCreator;
import org.batfish.main.Driver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class AllInOne {

  private static String[] getArgArrayFromString(String argString) {
    if (Strings.isNullOrEmpty(argString)) {
      return new String[0];
    }
    return argString.trim().split("\\s+");
  }

  private final String[] _args;

  private Client _client;

  private BatfishLogger _logger;

  private Settings _settings;

  public AllInOne(String[] args) {
    _args = args;
  }

  private boolean _exit = false;
  private static final String DEFAULT_SNAPSHOT_PREFIX = "ss_";
  public void run() {
    try {
      _settings = new Settings(_args);
    } catch (Exception e) {
      System.err.println("org.batfish.allinone: Initialization failed: " + e.getMessage());
      e.printStackTrace();
      System.exit(1);
    }

    String argString =
        String.format(
            "%s -%s %s -%s %s",
            _settings.getClientArgs(),
            org.batfish.client.config.Settings.ARG_LOG_LEVEL,
            _settings.getLogLevel(),
            org.batfish.client.config.Settings.ARG_RUN_MODE,
            _settings.getRunMode());

    if (_settings.getLogFile() != null) {
      argString +=
          String.format(
              " -%s %s", org.batfish.client.config.Settings.ARG_LOG_FILE, _settings.getLogFile());
    }

    if (_settings.getCommandFile() != null) {
      argString +=
          String.format(
              " -%s %s",
              org.batfish.client.config.Settings.ARG_COMMAND_FILE, _settings.getCommandFile());
    }

    if (_settings.getSnapshotDir() != null) {
      argString +=
          String.format(
              " -%s %s",
              org.batfish.client.config.Settings.ARG_SNAPSHOT_DIR, _settings.getSnapshotDir());
    }

    // if we are not running the client, we were like not specified a cmdfile.
    // lets do a dummy cmdfile do client initialization does not barf
    if (!_settings.getRunClient() && _settings.getCommandFile() == null) {
      argString +=
          String.format(
              " -%s %s", org.batfish.client.config.Settings.ARG_COMMAND_FILE, "dummy_allinone");
    }

    String[] initialArgArray = getArgArrayFromString(argString);
    List<String> clientArgs = new ArrayList<>(Arrays.asList(initialArgArray));
    final String[] argArray = clientArgs.toArray(new String[] {});

    try {
      _client = new Client(argArray);
      _logger = _client.getLogger();
      _logger.debugf("Started client with args: %s\n", Arrays.toString(argArray));
    } catch (Exception e) {
      System.err.printf(
          "Client initialization failed with args: %s\nExceptionMessage: %s\n",
          argString, e.getMessage());
      System.exit(1);
    }

    BindPortFutures bindPortFutures = runCoordinator();

    try {
      runBatfish();
    } catch (ExecutionException | InterruptedException e) {
      System.err.println("org.batfish.allinone: Worker initialization failed: " + e.getMessage());
      e.printStackTrace();
      System.exit(1);
    }

    if (_settings.getRunClient()) {
      try {
        _client.getSettings().setCoordinatorWorkV2Port(bindPortFutures.getWorkV2Port().get());
      } catch (ExecutionException | InterruptedException e) {
        System.err.println("org.batfish.allinone: Worker initialization failed: " + e.getMessage());
        e.printStackTrace();
        System.exit(1);
      }
      _client.run(new LinkedList<>());
      // The program does not terminate without it if the user misses the
      // quit command
      System.exit(0);
    } else {
      // sleep indefinitely, in chunks, since the client does not keep us
      // alive
      try {
        while (!_exit) {
          Thread.sleep(10 * 60 * 1000); // 10 minutes
          _logger.info("allinone: still alive ....\n");
        }
      } catch (Exception ex) {
        String stackTrace = Throwables.getStackTraceAsString(ex);
        System.err.println(stackTrace);
      }
    }
  }













//  public void mevRun() {
//    try {
//      _settings = new Settings(_args);
//    } catch (Exception e) {
//      System.err.println("org.batfish.allinone: Initialization failed: " + e.getMessage());
//      e.printStackTrace();
//      System.exit(1);
//    }
//
//    String argString =
//            String.format(
//                    "%s -%s %s -%s %s",
//                    _settings.getClientArgs(),
//                    org.batfish.client.config.Settings.ARG_LOG_LEVEL,
//                    _settings.getLogLevel(),
//                    org.batfish.client.config.Settings.ARG_RUN_MODE,
//                    _settings.getRunMode());
//
//    if (_settings.getLogFile() != null) {
//      argString +=
//              String.format(
//                      " -%s %s", org.batfish.client.config.Settings.ARG_LOG_FILE, _settings.getLogFile());
//    }
//
//    if (_settings.getCommandFile() != null) {
//      argString +=
//              String.format(
//                      " -%s %s",
//                      org.batfish.client.config.Settings.ARG_COMMAND_FILE, _settings.getCommandFile());
//    }
//
//    if (_settings.getSnapshotDir() != null) {
//      argString +=
//              String.format(
//                      " -%s %s",
//                      org.batfish.client.config.Settings.ARG_SNAPSHOT_DIR, _settings.getSnapshotDir());
//    }
//
//    // if we are not running the client, we were like not specified a cmdfile.
//    // lets do a dummy cmdfile do client initialization does not barf
//    if (!_settings.getRunClient() && _settings.getCommandFile() == null) {
//      argString +=
//              String.format(
//                      " -%s %s", org.batfish.client.config.Settings.ARG_COMMAND_FILE, "dummy_allinone");
//    }
//
//    String[] initialArgArray = getArgArrayFromString(argString);
//    List<String> clientArgs = new ArrayList<>(Arrays.asList(initialArgArray));
//    final String[] argArray = clientArgs.toArray(new String[] {});
//
//    try {
//      LineReader reader =
//              LineReaderBuilder.builder()
//                      .terminal(TerminalBuilder.builder().build())
//                      .completer(new ArgumentCompleter(new CommandCompleter(), new NullCompleter()))
//                      .build();
//      while (!_exit) {
//        try {
//          String rawLine = reader.readLine("MEV> ");
//          if (rawLine == null) {
//            break;
//          }
//          processCommand(rawLine);
//        } catch (UserInterruptException e) {
//          continue;
//        }
//      }
//    } catch (Exception e) {
//      System.err.printf(
//              "Client initialization failed with args: %s\nExceptionMessage: %s\n",
//              argString, e.getMessage());
//      System.exit(1);
//    }
//  }
//  public boolean processCommand(String command) {
//    String line = command.trim();
//    if (line.length() == 0 || line.startsWith("#")) {
//      return true;
//    }
//    _logger.debugf("Doing command: %s\n", line);
//    String[] words = line.split("\\s+");
//    return processCommand(words, null);
//  }
//
//  boolean processCommand(String[] words, @Nullable FileWriter outWriter) {
//    Command command;
//    try {
//      command = Command.fromName(words[0]);
//    } catch (BatfishException e) {
//      _logger.errorf("Command failed: %s\n", e.getMessage());
//      return false;
//    }
//
//    List<String> options = getCommandOptions(words);
//    List<String> parameters = getCommandParameters(words, options.size());
//
//    try {
//      return processCommand(command, words, outWriter, options, parameters);
//    } catch (Exception e) {
//      e.printStackTrace();
//      return false;
//    }
//  }
//
//  private List<String> getCommandOptions(String[] words) {
//    List<String> options = new LinkedList<>();
//
//    int currIndex = 1;
//
//    while (currIndex < words.length && words[currIndex].startsWith("-")) {
//      options.add(words[currIndex]);
//      currIndex++;
//    }
//
//    return options;
//  }
//
//  private List<String> getCommandParameters(String[] words, int numOptions) {
//    return Arrays.asList(words).subList(numOptions + 1, words.length);
//  }
//
//
//
//
//
//  private boolean processCommand(
//          Command command,
//          String[] words,
//          @Nullable FileWriter outWriter,
//          List<String> options,
//          List<String> parameters)
//          throws Exception {
//    switch (command) {
//      case INIT_SNAPSHOT:
//        return initSnapshot(outWriter, options, parameters, false);
//      case EXIT:
//      case QUIT:
//        return exit(options, parameters);
//
//      default:
//        _logger.errorf("Unsupported command %s\n", words[0]);
//        _logger.error("Type 'help' to see the list of valid commands\n");
//        return false;
//    }
//  }
//
//  private boolean initSnapshot(
//          @Nullable FileWriter outWriter,
//          List<String> options,
//          List<String> parameters,
//          boolean delta) {
//    Command command = delta ? Command.INIT_REFERENCE_SNAPSHOT : Command.INIT_SNAPSHOT;
//    if (!isValidArgument(options, parameters, 0, 1, 2, command)) {
//      return false;
//    }
//
//    String testrigLocation = parameters.get(0);
//    String testrigName =
//            (parameters.size() > 1) ? parameters.get(1) : DEFAULT_SNAPSHOT_PREFIX + UUID.randomUUID();
//
//    // initialize the container if it hasn't been init'd before
//    if (!isSetContainer(false)) {
//      _currContainerName = _workHelper.initNetwork(null, DEFAULT_NETWORK_PREFIX);
//      if (_currContainerName == null) {
//        _logger.errorf("Could not init network\n");
//        return false;
//      }
//      _logger.output("Init'ed and set active network");
//      _logger.infof(" to %s\n", _currContainerName);
//      _logger.output("\n");
//    }
//
//    if (!uploadTestrig(testrigLocation, testrigName)) {
//      unsetTestrig(delta);
//      return false;
//    }
//    _logger.output("Uploaded snapshot.\n");
//
//    _logger.output("Parsing now.\n");
//    WorkItem wItemParse = WorkItemBuilder.getWorkItemParse(_currContainerName, testrigName);
//
//    if (!delta) {
//      _currTestrig = testrigName;
//      _logger.infof("Current snapshot is now %s\n", _currTestrig);
//    } else {
//      _currDeltaTestrig = testrigName;
//      _logger.infof("Reference snapshot is now %s\n", _currDeltaTestrig);
//    }
//
//    return true;
//  }
//  private boolean isSetContainer(boolean printError) {
//    if (!_settings.getSanityCheck()) {
//      return true;
//    }
//
//    if (_currContainerName == null) {
//      if (printError) {
//        _logger.errorf("Active network is not set\n");
//      }
//      return false;
//    }
//
//    return true;
//  }
  private boolean exit(List<String> options, List<String> parameters) {
    if (!isValidArgument(options, parameters, 0, 0, 0, Command.EXIT)) {
      return false;
    }
    _exit = true;
    return true;
  }
  private boolean isValidArgument(
          List<String> options,
          List<String> parameters,
          int maxNumOptions,
          int minNumParas,
          int maxNumParas,
          Command command) {
    if (options.size() > maxNumOptions
            || (parameters.size() < minNumParas)
            || (parameters.size() > maxNumParas)) {
      _logger.errorf("Invalid arguments: %s %s\n", options, parameters);
      System.out.println(command);
      return false;
    }
    return true;
  }

  private void runBatfish() throws ExecutionException, InterruptedException {
    String batfishArgs =
        String.format(
            "%s -%s %s",
            _settings.getBatfishArgs(),
            org.batfish.config.Settings.ARG_RUN_MODE,
            _settings.getBatfishRunMode());

    String[] initialArgArray = getArgArrayFromString(batfishArgs);
    List<String> args = new ArrayList<>(Arrays.asList(initialArgArray));
    final String[] argArray = args.toArray(new String[] {});
    _logger.debugf("Starting batfish worker with args: %s\n", Arrays.toString(argArray));
    Thread thread =
        new Thread("batfishThread") {
          @Override
          public void run() {
            try {
              org.batfish.main.Driver.main(argArray, _logger);
            } catch (Exception e) {
              _logger.errorf(
                  "Initialization of batfish failed with args: %s\nExceptionMessage: %s\n",
                  Arrays.toString(argArray), e.getMessage());
            }
          }
        };
    thread.start();
  }

  private BindPortFutures runCoordinator() {
    String coordinatorArgs = _settings.getCoordinatorArgs();
    // If we are using a command file, just pick ephemeral ports to listen on
    if (_settings.getCommandFile() != null) {
      coordinatorArgs +=
          String.format(
              " -%s %s", org.batfish.coordinator.config.Settings.ARG_SERVICE_WORK_V2_PORT, 0);
    }
    String[] initialArgArray = getArgArrayFromString(coordinatorArgs);
    List<String> args = new ArrayList<>(Arrays.asList(initialArgArray));
    final String[] argArray = args.toArray(new String[] {});
    _logger.debugf("Starting coordinator with args: %s\n", Arrays.toString(argArray));

    BindPortFutures bindPortFutures = new BindPortFutures();
    Thread thread =
        new Thread("coordinatorThread") {
          @Override
          public void run() {
            WorkExecutorCreator workExecutorCreator =
                (logger, settings) ->
                    new BatfishWorkerServiceWorkExecutor(
                        logger, settings.getContainersLocation(), Driver.getBatfishWorkerService());
            try {
              org.batfish.coordinator.Main.main(
                  argArray, _logger, bindPortFutures, workExecutorCreator);
            } catch (Exception e) {
              _logger.errorf(
                  "Initialization of coordinator failed with args: %s\nExceptionMessage: %s\n",
                  Arrays.toString(argArray), e.getMessage());
            }
          }
        };

    thread.start();
    return bindPortFutures;
  }
}
